package calc.gateway;

import calc.common.CalcResult;
import calc.common.Heartbeat;
import calc.common.Topics;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Mỗi replica gateway dùng consumer group riêng (tên ngẫu nhiên) để nhận TẤT CẢ kết quả
 * và heartbeat, thay vì chia partition với các replica khác.
 */
@Component
public class ResultListener {

    private static final Logger log = LoggerFactory.getLogger(ResultListener.class);
    private static final String UNIQUE_GROUP = "calc-gateway-#{T(java.util.UUID).randomUUID().toString()}";

    private final PendingRequests pending;
    private final ServiceRegistry services;
    private final ObjectMapper mapper;

    public ResultListener(PendingRequests pending, ServiceRegistry services, ObjectMapper mapper) {
        this.pending = pending;
        this.services = services;
        this.mapper = mapper;
    }

    @KafkaListener(id = "gateway-results", idIsGroup = false, topics = Topics.RESULTS, groupId = UNIQUE_GROUP)
    public void onResult(String payload) {
        try {
            pending.complete(mapper.readValue(payload, CalcResult.class));
        } catch (Exception e) {
            log.warn("Bỏ qua kết quả không đọc được: {}", e.getMessage());
        }
    }

    @KafkaListener(id = "gateway-heartbeat", idIsGroup = false, topics = Topics.HEARTBEAT, groupId = UNIQUE_GROUP)
    public void onHeartbeat(String payload) {
        try {
            services.beat(mapper.readValue(payload, Heartbeat.class));
        } catch (Exception e) {
            log.warn("Bỏ qua heartbeat không đọc được: {}", e.getMessage());
        }
    }
}
