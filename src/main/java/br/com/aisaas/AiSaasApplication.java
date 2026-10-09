package br.com.aisaas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AiSaasApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiSaasApplication.class, args);
    }

}
