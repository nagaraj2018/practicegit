package loancalc.apps.controller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages={
		"loancalc.apps.controller","loancalc.apps.service"})
public class LoanCalculatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoanCalculatorApplication.class, args);
	}

}
