package calc.common;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Cấu hình chung cho các service tính toán: tạo topic và health indicator. */
@Configuration
@EnableScheduling
public class CalcCommonConfig {

    @Bean
    NewTopic requestTopic(@Value("${calc.operation}") String operation,
                          @Value("${calc.topic.partitions:3}") int partitions,
                          @Value("${calc.topic.replicas:1}") int replicas) {
        return TopicBuilder.name(Topics.requests(operation)).partitions(partitions).replicas(replicas).build();
    }

    @Bean
    NewTopic resultsTopic(@Value("${calc.topic.partitions:3}") int partitions,
                          @Value("${calc.topic.replicas:1}") int replicas) {
        return TopicBuilder.name(Topics.RESULTS).partitions(partitions).replicas(replicas).build();
    }

    @Bean
    NewTopic heartbeatTopic(@Value("${calc.topic.replicas:1}") int replicas) {
        return TopicBuilder.name(Topics.HEARTBEAT).partitions(1).replicas(replicas).build();
    }

    @Bean
    KafkaListenersHealthIndicator kafkaListenersHealthIndicator(KafkaListenerEndpointRegistry registry) {
        return new KafkaListenersHealthIndicator(registry, false);
    }
}
