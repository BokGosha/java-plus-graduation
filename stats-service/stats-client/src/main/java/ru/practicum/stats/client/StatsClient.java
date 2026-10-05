package ru.practicum.stats.client;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestClient;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class StatsClient {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final DiscoveryClient discoveryClient;
    private final RetryTemplate retryTemplate;
    private final String statsServiceId;
    private final String appName;
    private final RestClient restClient;

    public StatsClient(DiscoveryClient discoveryClient,
                       RetryTemplate retryTemplate,
                       String statsServiceId,
                       String appName,
                       Duration connectTimeout,
                       Duration readTimeout) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.discoveryClient = discoveryClient;
        this.retryTemplate = retryTemplate;
        this.statsServiceId = statsServiceId;
        this.appName = appName;
    }

    public void hit(HttpServletRequest request) {
        hit(new EndpointHitDto(
                appName,
                request.getRequestURI(),
                resolveIp(request),
                LocalDateTime.now().withNano(0)
        ));
    }

    public void hit(EndpointHitDto hit) {
        try {
            retryTemplate.execute(ctx -> restClient.post()
                    .uri(makeUri("/hit"))
                    .body(hit)
                    .retrieve()
                    .toBodilessEntity());
        } catch (Exception e) {
            log.warn("Не удалось отправить hit в сервис статистики: {}", e.getMessage());
        }
    }

    public List<ViewStatsDto> getStats(LocalDateTime start,
                                       LocalDateTime end,
                                       List<String> uris,
                                       boolean unique) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.newInstance()
                    .path("/stats")
                    .queryParam("start", start.format(FORMATTER))
                    .queryParam("end", end.format(FORMATTER))
                    .queryParam("unique", unique);

            if (uris != null && !uris.isEmpty()) {
                uris.forEach(uri -> builder.queryParam("uris", uri));
            }

            String path = builder.build().encode().toUriString();
            ViewStatsDto[] result = retryTemplate.execute(ctx -> restClient.get()
                    .uri(makeUri(path))
                    .retrieve()
                    .body(ViewStatsDto[].class));
            return result != null ? List.of(result) : List.of();
        } catch (Exception e) {
            log.warn("Не удалось получить статистику: {}", e.getMessage());
            return List.of();
        }
    }

    private String resolveIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private URI makeUri(String path) {
        ServiceInstance instance = getInstance();
        return URI.create("http://" + instance.getHost() + ":" + instance.getPort() + path);
    }

    private ServiceInstance getInstance() {
        List<ServiceInstance> instances = discoveryClient.getInstances(statsServiceId);
        if (instances == null || instances.isEmpty()) {
            throw new IllegalStateException(
                    "Сервис статистики с id=" + statsServiceId + " не найден в Eureka");
        }

        return instances.getFirst();
    }
}
