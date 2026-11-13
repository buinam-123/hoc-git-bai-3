package calc.gateway;

import calc.common.CalcResult;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

/** Giữ các yêu cầu HTTP đang chờ kết quả từ Kafka (ghép theo id). */
@Component
public class PendingRequests {

    private final ConcurrentHashMap<String, CompletableFuture<CalcResult>> waiting = new ConcurrentHashMap<>();

    public CompletableFuture<CalcResult> register(String id, long timeoutMs) {
        CompletableFuture<CalcResult> future = new CompletableFuture<>();
        waiting.put(id, future);
        future.orTimeout(timeoutMs, TimeUnit.MILLISECONDS);
        future.whenComplete((r, e) -> waiting.remove(id));
        return future;
    }

    /** Nhiều replica gateway cùng nhận mọi kết quả; replica nào không giữ id này thì bỏ qua. */
    public void complete(CalcResult result) {
        CompletableFuture<CalcResult> future = waiting.get(result.id());
        if (future != null) {
            future.complete(result);
        }
    }

    public void fail(String id, Throwable error) {
        CompletableFuture<CalcResult> future = waiting.get(id);
        if (future != null) {
            future.completeExceptionally(error);
        }
    }
}
