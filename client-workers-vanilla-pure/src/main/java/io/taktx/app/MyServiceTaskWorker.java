package io.taktx.app;

import com.fasterxml.jackson.databind.JsonNode;
import io.taktx.app.TaktXClientProvider.TestResultType;
import io.taktx.client.ExternalTaskTriggerConsumer;
import io.taktx.client.TaktXClient;
import io.taktx.dto.ExternalTaskTriggerDTO;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class MyServiceTaskWorker implements ExternalTaskTriggerConsumer {

  private final TaktXClient taktXClient;
  private static final Logger logger = Logger.getLogger(MyServiceTaskWorker.class.getName());

  public MyServiceTaskWorker(TaktXClient taktXClient) {
    this.taktXClient = taktXClient;
  }

  @Override
  public Set<String> getJobIds() {
    return Set.of("task1", "task2", "task3");
  }

  @Override
  public void acceptBatch(List<ExternalTaskTriggerDTO> externalTaskTriggers) {
    for (ExternalTaskTriggerDTO taskTrigger : externalTaskTriggers) {
      switch (taskTrigger.getExternalTaskId()) {
        case "task1":
          handleTask1(taskTrigger);
          break;
        case "task2":
          handleTask2(taskTrigger);
          break;
        case "task3":
          handleTask3(taskTrigger);
          break;
        default:
          logger.warning("Unknown external task id " + taskTrigger.getExternalTaskId());
          break;
      }
    }
  }

  // Worker method for task1
  private void handleTask1(ExternalTaskTriggerDTO taskTrigger) {
    int intVar = taskTrigger.getVariables().get("intVar").asInt();
    String stringVar = taskTrigger.getVariables().get("stringVar").asText();
    Map<String, JsonNode> allVariablesMap = taskTrigger.getVariables().getVariables();

    logger.info(
        String.format(
            """
          Task1 called with:
          intVar=%d
          stringVar=%s
          allVariablesMap=%s
        """,
            intVar, stringVar, allVariablesMap.toString()));

    taktXClient.respondToExternalTask(taskTrigger).respondSuccess(new TestResultType(123, "abc"));
  }

  // Worker method for task2
  private void handleTask2(ExternalTaskTriggerDTO taskTrigger) {
    int result3 = taskTrigger.getVariables().get("result3").asInt();
    String result4 = taskTrigger.getVariables().get("result4").asText();
    taktXClient
        .respondToExternalTask(taskTrigger)
        .respondSuccess(Map.of("result3", result3, "result4", result4));
  }

  // Worker method for task3
  private void handleTask3(ExternalTaskTriggerDTO taskTrigger) {
    int result3 = taskTrigger.getVariables().get("result3").asInt();
    String result4 = taskTrigger.getVariables().get("result4").asText();
    taktXClient
        .respondToExternalTask(taskTrigger)
        .respondSuccess(Map.of("result3", result3, "result4", result4));
  }
}
