package io.taktx.app;

import io.taktx.CleanupPolicy;
import io.taktx.client.AnnotationScanningExternalTaskTriggerConsumer;
import io.taktx.client.TaktXClient;
import java.io.IOException;
import java.util.Properties;

public class TaktXClientProvider {
  private static TaktXClient taktXClient = null;

  public static TaktXClient getTaktXClient() throws IOException {
    if (taktXClient == null) {
      // Minimal set of properties
      Properties properties = new Properties();
      properties.put("bootstrap.servers", "localhost:9092");
      properties.put("taktx.engine.namespace", "namespace");
      taktXClient = TaktXClient.newClientBuilder().withProperties(properties).build();

      taktXClient.deployTaktDeploymentAnnotatedClasses();

      AnnotationScanningExternalTaskTriggerConsumer externalTaskTriggerConsumer =
          new AnnotationScanningExternalTaskTriggerConsumer(
              taktXClient.getParameterResolverFactory(),
              taktXClient.getResultProcessorFactory(),
              taktXClient.getProcessInstanceResponder(),
              taktXClient.getExternalTaskTopicRequester(),
              3,
              CleanupPolicy.COMPACT,
              (short) 1);

      taktXClient.registerExternalTaskConsumer(
          externalTaskTriggerConsumer, "taktx-client-external-task-trigger-consumer");

      // Register the instance update consumer
      taktXClient.registerInstanceUpdateConsumer(
          "client-vanilla-annotations", new MyInstanceUpdateConsumer());
    }
    return taktXClient;
  }

  public static class TestResultType {
    private int result1;
    private String result2;

    public TestResultType(int intVar, String stringVar) {
      this.result1 = intVar;
      this.result2 = stringVar;
    }

    public int getResult1() {
      return result1;
    }

    public void setResult1(int result1) {
      this.result1 = result1;
    }

    public String getResult2() {
      return result2;
    }

    public void setResult2(String result2) {
      this.result2 = result2;
    }
  }
}
