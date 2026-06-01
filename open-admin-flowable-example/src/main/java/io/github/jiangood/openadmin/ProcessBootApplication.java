package io.github.jiangood.openadmin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

@SpringBootApplication
@EntityScan(basePackageClasses = ProcessBootApplication.class)
public class ProcessBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProcessBootApplication.class, args);
    }

}
