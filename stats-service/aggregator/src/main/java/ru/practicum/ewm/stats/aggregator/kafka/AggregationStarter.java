package ru.practicum.ewm.stats.aggregator.kafka;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.aggregator.service.EventSimilarityService;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {

    private final KafkaConsumerClient consumerClient;
    private final KafkaProducerClient producerClient;
    private final EventSimilarityService eventSimilarityService;

    @Value("${kafka.topics.user-actions}")
    private String userActionsTopic;

    @Value("${kafka.topics.events-similarity}")
    private String eventsSimilarityTopic;

    @Value("${kafka.consumer.poll-timeout-ms:1000}")
    private long pollTimeoutMs;

    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "aggregator-consumer");
        thread.setDaemon(true);
        return thread;
    });

    private volatile boolean running = true;

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        executor.submit(this::consume);
    }

    private void consume() {
        try {
            consumerClient.subscribe(userActionsTopic);

            while (running) {
                ConsumerRecords<Long, UserActionAvro> records = consumerClient.poll(Duration.ofMillis(pollTimeoutMs));

                if (records.isEmpty()) {
                    continue;
                }

                for (ConsumerRecord<Long, UserActionAvro> record : records) {
                    process(record.value());
                }

                producerClient.flush();
                consumerClient.commit();
            }
        } catch (WakeupException e) {
            log.info("Получен сигнал остановки");
        } catch (Exception e) {
            log.error("Ошибка в цикле обработки действий пользователей", e);
        } finally {
            commitQuietly();
        }
    }

    private void process(UserActionAvro action) {
        List<EventSimilarityAvro> similarities = eventSimilarityService.calculate(action);

        for (EventSimilarityAvro similarity : similarities) {
            producerClient.send(
                    eventsSimilarityTopic,
                    similarity.getTimestamp(),
                    String.valueOf(similarity.getEventA()),
                    similarity
            );
        }

        log.debug("Действие userId={}, eventId={} дало {} пересчитанных пар",
                action.getUserId(), action.getEventId(), similarities.size());
    }

    private void commitQuietly() {
        try {
            producerClient.flush();
            consumerClient.commit();
        } catch (Exception e) {
            log.warn("Не удалось зафиксировать смещения при остановке: {}", e.getMessage());
        }
    }

    @PreDestroy
    public void stop() throws InterruptedException {
        running = false;
        consumerClient.wakeup();

        executor.shutdown();
        if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
            executor.shutdownNow();
        }
    }
}
