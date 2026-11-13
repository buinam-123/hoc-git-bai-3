package calc.gateway;

import calc.common.CalcRequest;
import calc.common.CalcResult;
import calc.common.Instance;
import calc.common.KafkaListenersHealthIndicator;
import calc.common.Topics;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CalcController {

    private static final Logger log = LoggerFactory.getLogger(CalcController.class);

    private final OperationCatalog catalog;
    private final PendingRequests pending;
    private final ServiceRegistry services;
    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper mapper;
    private final KafkaListenersHealthIndicator readiness;
    private final long timeoutMs;

    public CalcController(OperationCatalog catalog, PendingRequests pending, ServiceRegistry services,
                          KafkaTemplate<String, String> kafka, ObjectMapper mapper,
                          KafkaListenersHealthIndicator readiness,
                          @Value("${calc.request-timeout-ms:8000}") long timeoutMs) {
        this.catalog = catalog;
        this.pending = pending;
        this.services = services;
        this.kafka = kafka;
        this.mapper = mapper;
        this.readiness = readiness;
        this.timeoutMs = timeoutMs;
    }

    @GetMapping("/operations")
    public List<Map<String, Object>> operations() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (OperationCatalog.Def d : catalog.all()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", d.id());
            m.put("label", d.label());
            m.put("symbol", d.symbol());
            m.put("arity", d.arity());
            m.put("topic", d.topic());
            out.add(m);
        }
        return out;
    }

    @GetMapping("/services")
    public Map<String, Object> services() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (OperationCatalog.Def d : catalog.all()) {
            int alive = services.aliveInstances(d.id());
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("op", d.id());
            m.put("service", d.id() + "-service");
            m.put("status", alive > 0 ? "up" : "down");
            m.put("instances", alive);
            list.add(m);
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("gateway", Instance.ID);
        body.put("services", list);
        return body;
    }

    @PostMapping("/calculate")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> calculate(
            @RequestBody(required = false) Map<String, Object> body) {

        Map<String, Object> in = body == null ? Map.of() : body;
        String op = text(in.get("op"));
        OperationCatalog.Def def = catalog.find(op);
        if (def == null) {
            return done(HttpStatus.BAD_REQUEST, error("Phép tính không hợp lệ: \"" + op + "\""));
        }
        if (!readiness.isReady()) {
            return done(HttpStatus.SERVICE_UNAVAILABLE, error("Gateway chưa kết nối xong với Kafka"));
        }

        String a = text(in.get("a"));
        String b = def.arity() == 2 ? text(in.get("b")) : null;
        String id = UUID.randomUUID().toString();
        long startedAt = System.currentTimeMillis();

        String json;
        try {
            json = mapper.writeValueAsString(new CalcRequest(id, op, a, b));
        } catch (JsonProcessingException e) {
            return done(HttpStatus.INTERNAL_SERVER_ERROR, error("Không tạo được yêu cầu"));
        }

        CompletableFuture<CalcResult> reply = pending.register(id, timeoutMs);
        kafka.send(def.topic(), id, json).whenComplete((sent, ex) -> {
            if (ex != null) {
                log.error("Gửi yêu cầu {} lỗi", id, ex);
                pending.fail(id, ex);
            }
        });

        return reply.handle((result, ex) -> {
            long totalMs = System.currentTimeMillis() - startedAt;
            if (ex != null) {
                Throwable cause = ex instanceof CompletionException && ex.getCause() != null ? ex.getCause() : ex;
                Map<String, Object> m = error(cause instanceof TimeoutException
                        ? def.label() + ": service không phản hồi trong " + (timeoutMs / 1000) + "s"
                        : "Không gửi được yêu cầu tới Kafka");
                m.put("id", id);
                m.put("op", op);
                m.put("totalMs", totalMs);
                return ResponseEntity
                        .status(cause instanceof TimeoutException ? HttpStatus.GATEWAY_TIMEOUT : HttpStatus.BAD_GATEWAY)
                        .body(m);
            }
            // Lỗi nghiệp vụ (chia cho 0, căn số âm...) vẫn là phản hồi hợp lệ → HTTP 200 với ok=false
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", id);
            m.put("op", op);
            m.put("a", a);
            if (b != null) {
                m.put("b", b);
            }
            m.put("ok", result.ok());
            if (result.ok()) {
                m.put("result", result.result());
            } else {
                m.put("error", result.error());
            }
            m.put("service", result.service());
            m.put("instance", result.instance());
            m.put("gateway", Instance.ID);
            m.put("topic", def.topic());
            m.put("processedMs", result.processedMs());
            m.put("totalMs", totalMs);
            return ResponseEntity.ok(m);
        });
    }

    private static String text(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static Map<String, Object> error(String message) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", false);
        m.put("error", message);
        return m;
    }

    private static CompletableFuture<ResponseEntity<Map<String, Object>>> done(HttpStatus status, Map<String, Object> body) {
        return CompletableFuture.completedFuture(ResponseEntity.status(status).body(body));
    }
}
