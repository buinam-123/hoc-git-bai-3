package calc.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Chỉ quét package calc.gateway (không quét calc.common vì đó là khung của các service tính toán). */
@SpringBootApplication(scanBasePackages = "calc.gateway")
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
