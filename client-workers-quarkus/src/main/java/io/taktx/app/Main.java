package io.taktx.app;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;
import io.taktx.client.annotation.Deployment;

@QuarkusMain
@Deployment(resources = "classpath:bpmn/*.bpmn")
public class Main {

  public static void main(String... args) {
    System.out.println("Running main method");
    Quarkus.run(args);
  }
}
