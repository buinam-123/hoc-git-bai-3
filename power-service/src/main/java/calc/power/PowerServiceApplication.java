package calc.power;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"calc.common", "calc.power"})
public class PowerServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PowerServiceApplication.class, args);
    }
}
