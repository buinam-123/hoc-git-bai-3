package calc.gateway;

import calc.common.Heartbeat;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/** Theo dõi nhịp tim của các service để biết service nào đang chạy và có bao nhiêu bản sao. */
@Component
public class ServiceRegistry {

    private static final long TTL_MS = 15_000;

    private final ConcurrentHashMap<String, Long> lastSeen = new ConcurrentHashMap<>();

    public void beat(Heartbeat heartbeat) {
        lastSeen.put(heartbeat.op() + "/" + heartbeat.instance(), System.currentTimeMillis());
    }

    public int aliveInstances(String op) {
        long now = System.currentTimeMillis();
        lastSeen.values().removeIf(ts -> now - ts > TTL_MS * 4);
        String prefix = op + "/";
        return (int) lastSeen.entrySet().stream()
                .filter(e -> e.getKey().startsWith(prefix) && now - e.getValue() < TTL_MS)
                .count();
    }
}
