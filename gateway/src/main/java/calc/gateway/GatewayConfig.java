package calc.gateway;

import calc.common.KafkaListenersHealthIndicator;
import calc.common.Topics;
import java.util.ArrayList;
import java.util.List;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class GatewayConfig {

    /** Gateway đảm bảo mọi topic tồn tại (tạo nếu chưa có). */
    @Bean
    KafkaAdmin.NewTopics calcTopics(OperationCatalog catalog,
                                    @Value("${calc.topic.partitions:3}") int partitions,
                                    @Value("${calc.topic.replicas:1}") int replicas) {
        List<NewTopic> topics = new ArrayList<>();
        for (OperationCatalog.Def def : catalog.all()) {
            topics.add(TopicBuilder.name(def.topic()).partitions(partitions).replicas(replicas).build());
        }
        topics.add(TopicBuilder.name(Topics.RESULTS).partitions(partitions).replicas(replicas).build());
        topics.add(TopicBuilder.name(Topics.HEARTBEAT).partitions(1).replicas(replicas).build());
        return new KafkaAdmin.NewTopics(topics.toArray(new NewTopic[0]));
    }

    /** Gateway chỉ sẵn sàng khi các listener đã được gán partition, để không bỏ lỡ kết quả đầu tiên. */
    @Bean
    KafkaListenersHealthIndicator kafkaListenersHealthIndicator(KafkaListenerEndpointRegistry registry) {
        return new KafkaListenersHealthIndicator(registry, true);
    }
}
