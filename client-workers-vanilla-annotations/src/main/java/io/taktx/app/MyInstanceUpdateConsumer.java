package io.taktx.app;

import io.taktx.client.InstanceUpdateRecord;
import java.util.List;
import java.util.function.Consumer;

public class MyInstanceUpdateConsumer implements Consumer<List<InstanceUpdateRecord>> {

  @Override
  public void accept(List<InstanceUpdateRecord> instanceUpdateRecords) {
    System.out.println("Received instance update records: " + instanceUpdateRecords);
  }
}
