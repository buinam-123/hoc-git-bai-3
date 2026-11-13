package calc.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Nhận yêu cầu từ topic calc.&lt;operation&gt;.requests, tính toán rồi ghi kết quả vào calc.results.
 * Các bản sao của cùng một service dùng chung group "calc-&lt;operation&gt;-service"
 * nên Kafka tự chia partition giữa chúng.
 */
@Component
public class RequestListener {

    private static final Logger log = LoggerFactory.getLogger(RequestListener.class);

    private final Operation operation;
    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper mapper;

    public RequestListener(Operation operation, KafkaTemplate<String, String> kafka, ObjectMapper mapper) {
        this.operation = operation;
        this.kafka = kafka;
        this.mapper = mapper;
    }

    @KafkaListener(
            id = "calc-request-listener",
            idIsGroup = false,
            topics = "calc.${calc.operation}.requests",
            groupId = "calc-${calc.operation}-service")
    public void onRequest(String payload) {
        long start = System.nanoTime();

        CalcRequest request;
        try {
            request = mapper.readValue(payload, CalcRequest.class);
        } catch (JsonProcessingException e) {
            log.warn("Bỏ qua message không phải JSON hợp lệ: {}", e.getOriginalMessage());
            return;
        }

        String service = operation.name() + "-service";
        boolean ok = true;
        String result = null;
        String error = null;
        try {
            result = operation.compute(request.a(), request.b());
        } catch (CalcException e) {
            ok = false;
            error = e.getMessage();
        } catch (RuntimeException e) {
            log.error("Lỗi không mong đợi khi xử lý {}", request.id(), e);
            ok = false;
            error = "Lỗi nội bộ trong " + service;
        }

        double processedMs = (System.nanoTime() - start) / 1_000_000.0;
        CalcResult response = new CalcResult(request.id(), operation.name(), service, Instance.ID,
                ok, result, error, processedMs);
        try {
            kafka.send(Topics.RESULTS, request.id(), mapper.writeValueAsString(response));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
        log.info("Đã xử lý {} ok={} {}ms", request.id(), ok, String.format("%.2f", processedMs));
    }
}
