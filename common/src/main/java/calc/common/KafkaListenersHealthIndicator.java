package calc.common;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;

/**
 * Readiness: tất cả Kafka listener đã chạy.
 * Với gateway còn yêu cầu đã được gán partition (để không mất kết quả đầu tiên).
 */
public class KafkaListenersHealthIndicator extends AbstractHealthIndicator {

    private final KafkaListenerEndpointRegistry registry;
    private final boolean requireAssignment;

    public KafkaListenersHealthIndicator(KafkaListenerEndpointRegistry registry, boolean requireAssignment) {
        super("Kafka listener health check failed");
        this.registry = registry;
        this.requireAssignment = requireAssignment;
    }

    public boolean isReady() {
        var containers = registry.getListenerContainers();
        if (containers.isEmpty()) {
            return false;
        }
        for (MessageListenerContainer c : containers) {
            if (!c.isRunning()) {
                return false;
            }
            if (requireAssignment) {
                var assigned = c.getAssignedPartitions();
                if (assigned == null || assigned.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        if (isReady()) {
            builder.up();
        } else {
            builder.down().withDetail("reason", "Kafka listener chưa sẵn sàng");
        }
    }
}
