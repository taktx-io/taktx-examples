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
import io.quarkus.scheduler.Scheduled;
import io.taktx.client.TaktXClient;
import io.taktx.dto.VariablesDTO;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

@Path("/processes")
@Slf4j
public class ProcessResource {
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Inject TaktXClient taktClient;

  private final Map<String, Job> jobs = new HashMap<>();

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
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
  public void startProcessInstance(
      @PathParam("process") String process, @PathParam("count") int count, String payload)
      throws JsonProcessingException {
    JsonNode jsonNode = objectMapper.readTree(payload);

    VariablesDTO variables = VariablesDTO.empty();
    jsonNode.fieldNames().forEachRemaining(key -> variables.put(key, jsonNode.get(key)));
    for (int i = 0; i < count; i++) {
      taktClient.startProcess(process, variables);
    }
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

  @Scheduled(every = "1s")
  void increment() {
    jobs.forEach(
        (key, value) -> {
          log.info("Starting {} jobs {}", value.count, value.process);
          for (int i = 0; i < value.count; i++) {
            taktClient.startProcess(value.process, value.variables);
          }
        });
  }

  private record Job(String process, int count, VariablesDTO variables) {}
}
