package ru.practicum.event;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// Категории лежат вне пакета приложения, а JPA по умолчанию ищет сущности и репозитории только в нём
@SpringBootApplication(scanBasePackages = "ru.practicum")
@EntityScan(basePackages = {"ru.practicum.event", "ru.practicum.category"})
@EnableJpaRepositories(basePackages = {"ru.practicum.event", "ru.practicum.category"})
@EnableFeignClients(basePackages = "ru.practicum.event.client")
public class EventServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventServiceApplication.class, args);
    }
}
