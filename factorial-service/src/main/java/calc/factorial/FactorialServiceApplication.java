package calc.factorial;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"calc.common", "calc.factorial"})
public class FactorialServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FactorialServiceApplication.class, args);
    }
}
