package calc.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Gửi nhịp tim mỗi 5 giây để frontend hiển thị trạng thái service. */
@Component
public class HeartbeatPublisher {

    private static final Logger log = LoggerFactory.getLogger(HeartbeatPublisher.class);

    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper mapper;
    private final String operation;

    public HeartbeatPublisher(KafkaTemplate<String, String> kafka, ObjectMapper mapper,
                              @Value("${calc.operation}") String operation) {
        this.kafka = kafka;
        this.mapper = mapper;
        this.operation = operation;
    }

    @Scheduled(initialDelay = 2000, fixedRate = 5000)
    public void beat() {
        try {
            String json = mapper.writeValueAsString(new Heartbeat(operation, Instance.ID, System.currentTimeMillis()));
            kafka.send(Topics.HEARTBEAT, operation, json);
        } catch (Exception e) {
            log.warn("Gửi heartbeat lỗi: {}", e.getMessage());
        }
    }
}
