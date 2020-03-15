package au.com.nab.fx.challenge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAutoConfiguration
public class FXCalculatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(FXCalculatorApplication.class, args);
	}

}
