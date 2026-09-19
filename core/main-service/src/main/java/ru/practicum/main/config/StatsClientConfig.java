package ru.practicum.main.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.stats.client.StatsClient;

@Configuration
public class StatsClientConfig {

    @Bean
    public StatsClient statsClient(@Value("${stats-server.url:lb://stats-server}") String statsServerUrl) {
        return new StatsClient(statsServerUrl);
    }

    @Bean
    public String appName(@Value("${spring.application.name:main-service}") String appName) {
        return appName;
    }
}
