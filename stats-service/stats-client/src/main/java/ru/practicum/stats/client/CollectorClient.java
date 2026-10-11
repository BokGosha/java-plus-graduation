package ru.practicum.stats.client;

import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.UserActionProto;

import java.time.Instant;

@Slf4j
public class CollectorClient {

    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub client;

    public void collectView(long userId, long eventId) {
        collect(userId, eventId, ActionTypeProto.ACTION_VIEW);
    }

    public void collectRegister(long userId, long eventId) {
        collect(userId, eventId, ActionTypeProto.ACTION_REGISTER);
    }

    public void collectLike(long userId, long eventId) {
        collect(userId, eventId, ActionTypeProto.ACTION_LIKE);
    }

    private void collect(long userId, long eventId, ActionTypeProto actionType) {
        Instant now = Instant.now();

        UserActionProto action = UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(actionType)
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(now.getEpochSecond())
                        .setNanos(now.getNano())
                        .build())
                .build();

        log.debug("Отправляем действие в collector: userId={}, eventId={}, type={}", userId, eventId, actionType);

        client.collectUserAction(action);
    }
}
