package ru.practicum.ewm.stats.analyzer.kafka;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.analyzer.service.EventSimilarityService;
import ru.practicum.ewm.stats.analyzer.service.UserActionService;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyzerStarter {

    private final UserActionConsumerClient userActionConsumerClient;
    private final EventSimilarityConsumerClient eventSimilarityConsumerClient;

    private final UserActionService userActionService;
    private final EventSimilarityService eventSimilarityService;

    @Value("${kafka.topics.user-actions}")
    private String userActionsTopic;

    @Value("${kafka.topics.events-similarity}")
    private String eventsSimilarityTopic;

    @Value("${kafka.consumer-user-actions.poll-timeout-ms:1000}")
    private long userActionsPollTimeoutMs;

    @Value("${kafka.consumer-events-similarity.poll-timeout-ms:1000}")
    private long eventsSimilarityPollTimeoutMs;

    private final ExecutorService executor = Executors.newFixedThreadPool(2, runnable -> {
        Thread thread = new Thread(runnable, "analyzer-consumer");
        thread.setDaemon(true);
        return thread;
    });

    private volatile boolean running = true;

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        executor.submit(() -> consume(
                userActionConsumerClient, userActionsTopic, userActionsPollTimeoutMs, userActionService::save));
        executor.submit(() -> consume(
                eventSimilarityConsumerClient, eventsSimilarityTopic, eventsSimilarityPollTimeoutMs,
                eventSimilarityService::save));
    }

    private <K, T extends SpecificRecordBase> void consume(BaseConsumerClient<K, T> client,
                                                          String topic,
                                                          long pollTimeoutMs,
                                                          Consumer<T> handler) {
        try {
            client.subscribe(topic);

            while (running) {
                ConsumerRecords<K, T> records = client.poll(Duration.ofMillis(pollTimeoutMs));

                if (records.isEmpty()) {
                    continue;
                }

                for (ConsumerRecord<K, T> record : records) {
                    handler.accept(record.value());
                }

                client.commit();
            }
        } catch (WakeupException e) {
            log.info("Получен сигнал остановки для топика {}", topic);
        } catch (Exception e) {
            log.error("Ошибка в цикле обработки топика {}", topic, e);
        } finally {
            commitQuietly(client, topic);
        }
    }

    private void commitQuietly(BaseConsumerClient<?, ?> client, String topic) {
        try {
            client.commit();
        } catch (Exception e) {
            log.warn("Не удалось зафиксировать смещения топика {}: {}", topic, e.getMessage());
        }
    }

    @PreDestroy
    public void stop() throws InterruptedException {
        running = false;
        userActionConsumerClient.wakeup();
        eventSimilarityConsumerClient.wakeup();

        executor.shutdown();
        if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
            executor.shutdownNow();
        }
    }
}
