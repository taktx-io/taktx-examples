package io.taktx.app.instanceupdates;

import io.taktx.client.InstanceUpdateRecord;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class EventObserver {
  Logger logger = LoggerFactory.getLogger(EventObserver.class);

  public void onMyEvent(@Observes InstanceUpdateRecord instanceUpdateRecord) {
    logger.info(
        "Observer received {} {}",
        instanceUpdateRecord.getProcessInstanceId(),
        instanceUpdateRecord.getUpdate());
  }
}
