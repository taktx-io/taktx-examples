/*
 * TaktX - A high-performance BPMN engine
 * Copyright (c) 2025 Eric Hendriks All rights reserved.
 * This file is part of TaktX, licensed under the TaktX Business Source License v1.0.
 * Free use is permitted with up to 3 Kafka partitions per topic. See LICENSE file for details.
 * For commercial use or more partitions and features, contact [https://www.taktx.io/contact].
 */

package io.taktx.app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.taktx.client.TaktXClient;
import io.taktx.dto.VariablesDTO;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

@Path("/processes")
public class ProcessResource {
  private static final Logger LOG = Logger.getLogger(ProcessResource.class.getName());
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Inject TaktXClient taktClient;
  @Inject ScheduledExecutorService scheduler;

  private final Map<String, Job> jobs = new HashMap<>();
  private final Map<String, Integer> remainders = new HashMap<>();

  void onStart(@Observes StartupEvent ev) {
    scheduler = Executors.newSingleThreadScheduledExecutor();
    scheduler.scheduleAtFixedRate(this::increment, 0, 100, TimeUnit.MILLISECONDS);
  }

  void onStop(@Observes ShutdownEvent ev) {
    if (scheduler != null) {
      scheduler.shutdown();
    }
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{process}/{count}")
  @RequestBody(
      required = false, // so Swagger UI does NOT say "Request body required"
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema =
                  @Schema(
                      type = SchemaType.OBJECT,
                      example = "{}" // default body shown in the editor
                      ),
              examples = {
                @ExampleObject(name = "Empty", value = "{}"),
                @ExampleObject(
                    name = "Random",
                    value =
                        """
        { "foo": "bar", "count": 3, "flags": ["a","b"], "meta": { "priority": true } }
      """)
              }))
  public List<UUID> startProcessInstance(
      @PathParam("process") String process, @PathParam("count") int count, String payload)
      throws JsonProcessingException {
    JsonNode jsonNode = objectMapper.readTree(payload);

    VariablesDTO variables = VariablesDTO.empty();
    jsonNode.fieldNames().forEachRemaining(key -> variables.put(key, jsonNode.get(key)));
    List<UUID> processInstanceIds = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      UUID uuid = taktClient.startProcess(process, variables);
      processInstanceIds.add(uuid);
    }
    return processInstanceIds;
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/jobs/{process}/{count}")
  @Operation(summary = "Create jobs")
  @RequestBody(
      required = false, // so Swagger UI does NOT say "Request body required"
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema =
                  @Schema(
                      type = SchemaType.OBJECT,
                      example = "{}" // default body shown in the editor
                      ),
              examples = {
                @ExampleObject(name = "Empty", value = "{}"),
                @ExampleObject(
                    name = "Random",
                    value =
                        """
        { "foo": "bar", "count": 3, "flags": ["a","b"], "meta": { "priority": true } }
      """)
              }))
  public Job startJob(
      @PathParam("process") String process, @PathParam("count") int count, String payload) {
    JsonNode jsonNode = objectMapper.convertValue(payload, JsonNode.class);
    VariablesDTO variables = VariablesDTO.empty();
    jsonNode.fieldNames().forEachRemaining(key -> variables.put(key, jsonNode.get(key)));

    Job job = new Job(process, count, variables);
    jobs.put(job.process, job);
    return job;
  }

  @DELETE
  @Path("/jobs/{process}")
  public Job stopJob(@PathParam("process") String process) {
    return jobs.remove(process);
  }

  @GET
  @Path("/jobs")
  public Set<Job> getJobs() {
    return Set.copyOf(jobs.values());
  }

  void increment() {
    jobs.forEach(
        (key, value) -> {
          // Calculate instances to start this cycle (every 100ms)
          int remainder = remainders.getOrDefault(key, 0);
          int totalToProcess = value.count + remainder;
          int instancesToStart = totalToProcess / 10;
          remainders.put(key, totalToProcess % 10);

          LOG.info(String.format("Starting %d jobs %s", instancesToStart, value.process));
          for (int i = 0; i < instancesToStart; i++) {
            taktClient.startProcess(value.process, value.variables);
          }
        });
  }

  private record Job(String process, int count, VariablesDTO variables) {}
}
