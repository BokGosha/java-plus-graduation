package ru.practicum.ewm.stats.analyzer.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.event.GrpcServerStartedEvent;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrpcPortMetadataConfigurer {

    private static final String GRPC_PORT_METADATA_KEY = "gRPC_port";

    private final EurekaInstanceConfigBean eurekaInstanceConfig;

    @EventListener
    public void onGrpcServerStarted(GrpcServerStartedEvent event) {
        eurekaInstanceConfig.getMetadataMap().put(GRPC_PORT_METADATA_KEY, String.valueOf(event.getPort()));
        log.info("gRPC-сервер слушает порт {}, порт опубликован в метаданных Eureka", event.getPort());
    }
}
