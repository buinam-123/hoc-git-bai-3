package calc.gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import calc.common.CalcResult;
import calc.common.Heartbeat;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;

class PendingRequestsTest {

    @Test
    void completesMatchingId() throws Exception {
        PendingRequests pending = new PendingRequests();
        CompletableFuture<CalcResult> f = pending.register("abc", 5000);
        pending.complete(new CalcResult("other", "add", "add-service", "pod", true, "1", null, 0.1)); // id khác → bỏ qua
        assertTrue(!f.isDone());
        pending.complete(new CalcResult("abc", "add", "add-service", "pod", true, "5", null, 0.1));
        assertEquals("5", f.get().result());
    }

    @Test
    void timesOut() {
        PendingRequests pending = new PendingRequests();
        CompletableFuture<CalcResult> f = pending.register("slow", 50);
        ExecutionException e = assertThrows(ExecutionException.class, f::get);
        assertTrue(e.getCause() instanceof TimeoutException);
    }

    @Test
    void countsAliveInstances() {
        ServiceRegistry registry = new ServiceRegistry();
        assertEquals(0, registry.aliveInstances("add"));
        registry.beat(new Heartbeat("add", "pod-1", 0));
        registry.beat(new Heartbeat("add", "pod-2", 0));
        registry.beat(new Heartbeat("sqrt", "pod-9", 0));
        assertEquals(2, registry.aliveInstances("add"));
        assertEquals(1, registry.aliveInstances("sqrt"));
    }
}
