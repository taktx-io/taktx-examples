/*
 * TaktX - A high-performance BPMN engine
 * Copyright (c) 2025 Eric Hendriks All rights reserved.
 * This file is part of TaktX, licensed under the TaktX Business Source License v1.0.
 * Free use is permitted with up to 3 Kafka partitions per topic. See LICENSE file for details.
 * For commercial use or more partitions and features, contact [https://www.taktx.io/contact].
 */

package io.taktx.app;

import io.taktx.client.TaktXClient;
import io.taktx.dto.ProcessDefinitionDTO;
import io.taktx.dto.ProcessDefinitionKey;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Path("/processdefinitions")
public class DefinitionResource {
  private static final Logger LOG = Logger.getLogger(DefinitionResource.class.getName());

  private final TaktXClient taktXClient;

  public DefinitionResource(TaktXClient taktXClient) {
    this.taktXClient = taktXClient;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAllProcessDefinitions(@QueryParam("id") String processDefinitionId) {
    try {
      LOG.info("Retrieving process definitions " + processDefinitionId);
      Map<ProcessDefinitionKey, ProcessDefinitionDTO> definitions;
      if (processDefinitionId != null && !processDefinitionId.isEmpty()) {
        definitions =
            taktXClient
                .getProcessDefinitionConsumer()
                .getDeployedProcessDefinitions(processDefinitionId);
      } else {
        definitions = taktXClient.getProcessDefinitionConsumer().getDeployedProcessDefinitions();
      }

      // Group definitions by process definition ID
      Map<String, Map<Integer, ProcessDefinitionDTO>> groupedDefinitions =
          definitions.entrySet().stream()
              .collect(
                  Collectors.groupingBy(
                      entry -> entry.getKey().getProcessDefinitionId(),
                      Collectors.toMap(entry -> entry.getKey().getVersion(), Map.Entry::getValue)));

      return Response.ok(groupedDefinitions).build();
    } catch (Exception e) {
      LOG.severe("Error retrieving process definitions" + e.getMessage());
      return Response.serverError().entity("Error retrieving process definitions").build();
    }
  }

  @GET
  @Path("/{processDefinitionId}")
  public String startProcessInstance(
      @PathParam("processDefinitionId") String processDefinitionIdVersionString)
      throws IOException {
    String[] split = processDefinitionIdVersionString.split("\\.");
    String processDefinitionId = split[0];
    Integer version = Integer.parseInt(split[1]);
    ProcessDefinitionKey processDefinitionKey =
        new ProcessDefinitionKey(processDefinitionId, version);
    return taktXClient.getProcessDefinitionXml(processDefinitionKey);
  }
}
