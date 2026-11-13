package calc.divide;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"calc.common", "calc.divide"})
public class DivideServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DivideServiceApplication.class, args);
    }
}
