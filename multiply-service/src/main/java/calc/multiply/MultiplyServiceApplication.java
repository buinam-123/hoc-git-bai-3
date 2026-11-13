package calc.multiply;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"calc.common", "calc.multiply"})
public class MultiplyServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MultiplyServiceApplication.class, args);
    }
}
