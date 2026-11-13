package calc.sqrt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"calc.common", "calc.sqrt"})
public class SqrtServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SqrtServiceApplication.class, args);
    }
}
