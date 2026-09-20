package ru.practicum.main.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import ru.practicum.stats.client.StatsClient;

@Configuration
public class StatsClientConfig {

    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder(RestClient.Builder builder) {
        return builder;
    }

    @Bean
    public StatsClient statsClient(
            @Value("${stats-server.url:lb://stats-server}") String statsServerUrl,
            @org.springframework.beans.factory.annotation.Qualifier("loadBalancedRestClientBuilder") RestClient.Builder restClientBuilder
    ) {
        return new StatsClient(restClientBuilder, statsServerUrl);
    }

    @Bean
    public String appName(@Value("${spring.application.name:main-service}") String appName) {
        return appName;
    }
}
