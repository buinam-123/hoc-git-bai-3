package calc.subtract;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"calc.common", "calc.subtract"})
public class SubtractServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SubtractServiceApplication.class, args);
    }
}
