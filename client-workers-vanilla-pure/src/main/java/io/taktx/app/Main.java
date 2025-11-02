package io.taktx.app;

import io.taktx.client.TaktXClient;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;

public class Main {

  public static void main(String... args) throws InterruptedException, IOException {

    TaktXClient taktXClient = TaktXClientProvider.getTaktXClient();
    taktXClient.start();

    waitUntilClosed(taktXClient);
  }

  private static void waitUntilClosed(TaktXClient taktXClient) throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  try {
                    // try to stop gracefully if a stop() method exists
                    try {
                      Method stop = taktXClient.getClass().getMethod("stop");
                      stop.invoke(taktXClient);
                    } catch (NoSuchMethodException ignored) {
                      // no stop method available
                    }
                  } catch (Exception ignored) {
                  } finally {
                    latch.countDown();
                  }
                },
                "shutdown-hook"));

    // block main thread until shutdown hook runs
    latch.await();
  }
}
