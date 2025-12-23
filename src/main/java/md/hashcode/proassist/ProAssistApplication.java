package md.hashcode.proassist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = "md.hashcode")
public class ProAssistApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProAssistApplication.class, args);
    }

}
