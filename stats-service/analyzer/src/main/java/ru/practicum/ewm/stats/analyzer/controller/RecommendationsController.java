package ru.practicum.ewm.stats.analyzer.controller;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.analyzer.model.RecommendedEvent;
import ru.practicum.ewm.stats.analyzer.service.RecommendationService;
import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendationsControllerGrpc;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.UserPredictionsRequestProto;

import java.util.List;
import java.util.function.Supplier;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class RecommendationsController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final RecommendationService recommendationService;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request,
                                          StreamObserver<RecommendedEventProto> responseObserver) {
        log.debug("Запрошены рекомендации: userId={}, maxResults={}", request.getUserId(), request.getMaxResults());

        stream(() -> recommendationService.getRecommendationsForUser(
                request.getUserId(), request.getMaxResults()), responseObserver);
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request,
                                 StreamObserver<RecommendedEventProto> responseObserver) {
        log.debug("Запрошены похожие мероприятия: eventId={}, userId={}, maxResults={}",
                request.getEventId(), request.getUserId(), request.getMaxResults());

        stream(() -> recommendationService.getSimilarEvents(
                request.getEventId(), request.getUserId(), request.getMaxResults()), responseObserver);
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request,
                                     StreamObserver<RecommendedEventProto> responseObserver) {
        log.debug("Запрошено количество взаимодействий: eventIds={}", request.getEventIdList());

        stream(() -> recommendationService.getInteractionsCount(request.getEventIdList()), responseObserver);
    }

    private void stream(Supplier<List<RecommendedEvent>> supplier,
                        StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            for (RecommendedEvent recommended : supplier.get()) {
                responseObserver.onNext(toProto(recommended));
            }

            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Ошибка при подготовке рекомендаций", e);
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }

    private RecommendedEventProto toProto(RecommendedEvent recommended) {
        return RecommendedEventProto.newBuilder()
                .setEventId(recommended.eventId())
                .setScore(recommended.score())
                .build();
    }
}
