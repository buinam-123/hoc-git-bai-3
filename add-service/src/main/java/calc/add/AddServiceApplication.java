package calc.add;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"calc.common", "calc.add"})
public class AddServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AddServiceApplication.class, args);
    }
}
