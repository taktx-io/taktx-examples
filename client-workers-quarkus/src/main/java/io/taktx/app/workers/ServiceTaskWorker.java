package io.taktx.app.workers;

import io.quarkus.runtime.Startup;
import io.taktx.client.ExternalTaskInstanceResponder;
import io.taktx.client.annotation.JobWorker;
import io.taktx.client.annotation.Variable;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Startup
@ApplicationScoped
public class ServiceTaskWorker {
  private static final Logger logger = LoggerFactory.getLogger(ServiceTaskWorker.class);

  /**
   * A typical worker method demonstrating various parameter types, including primitive types, maps,
   * deserialized objects, and lists.
   *
   * <p>The worker is marked as autoComplete=true, so the task will be automatically completed upon
   * return
   *
   * @param intVar an integer variable
   * @param stringVar a string variable
   * @param allVariablesMap a map containing all variables
   * @param singleVarMap a map variable extracted using @Variable annotation
   * @param deserializedVar a deserialized object of type TestType extracted using @Variable
   * @param listVar a list of strings
   */
  @JobWorker(taskId = "task1", autoComplete = true)
  public TestResultType task1(
      int intVar,
      String stringVar,
      Map<String, Object> allVariablesMap,
      @Variable("mapVar") Map<String, Object> singleVarMap,
      @Variable("mapVar") TestType deserializedVar,
      List<String> listVar) {
    logger.info(
        """
        Task1 called with:
        intVar={}
        stringVar={}
        allVariablesMap={}
        mapVar={}
        deserializedVar={}
        listVar={}
        """,
        intVar,
        stringVar,
        allVariablesMap,
        singleVarMap,
        deserializedVar,
        listVar);
    return new TestResultType(intVar, stringVar);
  }

  /**
   * A second worker method that takes the results from task1 and returns a map of new results.
   *
   * @param result1
   * @param result2
   * @return Map object containing result3 and result4
   */
  @JobWorker(taskId = "task2", autoComplete = true)
  public Map<String, Object> task2(int result1, String result2) {
    logger.info("Task2 called with: result1={}, result2={}", result1, result2);
    return Map.of("result3", result1, "result4", result2);
  }

  /**
   * A third worker method that takes the results from task2 and has autoComplete set to false. This
   * means that the task will not be automatically completed upon return, and the method must
   * explicitly call the ExternalTaskInstanceResponder to complete the task. Normally you will call
   * respondSuccess(), but there are also methods to respond with error, escalation, or promise.
   *
   * @param result3 the integer result from task2
   * @param result4 the string result from task2
   * @param externalTaskInstanceResponder the responder to use to complete the task
   */
  @JobWorker(taskId = "task3", autoComplete = false)
  public void task2(
      int result3, String result4, ExternalTaskInstanceResponder externalTaskInstanceResponder) {
    logger.info("Task3 called with: result3={}, result4={}", result3, result4);
    externalTaskInstanceResponder.respondSuccess(new TestResultType(result3, result4));
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

  public static class TestType {
    private int field1;
    private String field2;

    public int getField1() {
      return field1;
    }

    public void setField1(int field1) {
      this.field1 = field1;
    }

    public String getField2() {
      return field2;
    }

    public void setField2(String field2) {
      this.field2 = field2;
    }

    @Override
    public String toString() {
      return "field1:" + field1 + ", field2:" + field2;
    }
  }
}
