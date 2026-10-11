package ru.practicum.ewm.stats.collector.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.collector.mapper.UserActionProtoMapper;
import ru.practicum.ewm.stats.collector.service.UserActionService;
import ru.practicum.ewm.stats.proto.UserActionControllerGrpc;

@GrpcService
@RequiredArgsConstructor
public class UserActionController extends UserActionControllerGrpc.UserActionControllerImplBase {

    private final UserActionProtoMapper userActionProtoMapper;
    private final UserActionService userActionService;

    @Override
    public void collectUserAction(ru.practicum.ewm.stats.proto.UserActionProto request,
                                  StreamObserver<Empty> responseObserver) {
        try {
            userActionService.collect(userActionProtoMapper.mapUserActionProto(request));
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }
}
