package ru.practicum.stats.client.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.MaxAttemptsRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import ru.practicum.stats.client.StatsClient;

import java.time.Duration;

@AutoConfiguration
public class StatsClientConfig {

    @Bean
    @ConditionalOnMissingBean(name = "statsRetryTemplate")
    public RetryTemplate statsRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(300L);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        RetryPolicy retryPolicy = new MaxAttemptsRetryPolicy(3);
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }

    @Bean
    @ConditionalOnMissingBean
    public StatsClient statsClient(DiscoveryClient discoveryClient,
                                   RetryTemplate statsRetryTemplate,
                                   @Value("${stats.service-id:stats-server}") String statsServiceId,
                                   @Value("${spring.application.name}") String appName,
                                   @Value("${stats.connect-timeout:1s}") Duration connectTimeout,
                                   @Value("${stats.read-timeout:3s}") Duration readTimeout) {
        return new StatsClient(discoveryClient, statsRetryTemplate, statsServiceId, appName,
                connectTimeout, readTimeout);
    }
}
