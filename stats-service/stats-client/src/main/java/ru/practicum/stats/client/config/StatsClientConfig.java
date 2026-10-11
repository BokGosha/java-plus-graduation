package ru.practicum.stats.client.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import ru.practicum.stats.client.AnalyzerClient;
import ru.practicum.stats.client.CollectorClient;

@AutoConfiguration
public class StatsClientConfig {

    @Bean
    @ConditionalOnMissingBean
    public CollectorClient collectorClient() {
        return new CollectorClient();
    }

    @Bean
    @ConditionalOnMissingBean
    public AnalyzerClient analyzerClient() {
        return new AnalyzerClient();
    }
}
