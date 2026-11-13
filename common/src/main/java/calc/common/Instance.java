package calc.common;

import java.net.InetAddress;

/** Tên của tiến trình hiện tại (tên pod/container khi chạy trong Docker hoặc Kubernetes). */
public final class Instance {
    public static final String ID = resolve();

    private Instance() {}

    private static String resolve() {
        String host = System.getenv("HOSTNAME");
        if (host != null && !host.isBlank()) {
            return host;
        }
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "local";
        }
    }
}
