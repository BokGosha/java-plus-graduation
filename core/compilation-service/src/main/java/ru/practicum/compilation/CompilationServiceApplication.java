package ru.practicum.compilation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "ru.practicum")
@EnableFeignClients(basePackages = "ru.practicum.compilation.client")
public class CompilationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CompilationServiceApplication.class, args);
    }
}
