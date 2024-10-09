package org.delivery.storeadmin.domain.sse.connection.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.delivery.storeadmin.domain.sse.connection.ifs.ConnectionPoolIfs;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Getter
@ToString
@EqualsAndHashCode
public class UserSseConnection {
    private final String uniqueKey;
    private final SseEmitter sseEmitter;
    private final ObjectMapper objectMapper;
    private final ConnectionPoolIfs<String, UserSseConnection> connectionPoolIfs;

    public UserSseConnection(String uniqueKey, ObjectMapper objectMapper, ConnectionPoolIfs<String, UserSseConnection> connectionPoolIfs) {
        this.uniqueKey = uniqueKey;
        this.objectMapper = objectMapper;
        this.connectionPoolIfs = connectionPoolIfs;
        this.sseEmitter = new SseEmitter(1000L * 60);

        this.sseEmitter.onCompletion(() -> {
            //connection pool remove
        });

        this.sseEmitter.onTimeout(() -> {
            this.sseEmitter.complete();
            connectionPoolIfs.conCompletionCallback(this);
        });

        this.sendMessage("onopen", "connect");
    }

    public static UserSseConnection connect(
            String uniqueKey,
            ConnectionPoolIfs<String, UserSseConnection> connectionPoolIfs,
            ObjectMapper objectMapper
    ) {
        return new UserSseConnection(uniqueKey, objectMapper, connectionPoolIfs);
    }

    public void sendMessage(String eventName, Object data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            SseEmitter.SseEventBuilder event = SseEmitter.event()
                    .name(eventName)
                    .data(json);
            this.sseEmitter.send(event);
        } catch (IOException e) {
            this.sseEmitter.completeWithError(e);
        }
    }

    public void sendMessage(Object data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            SseEmitter.SseEventBuilder event = SseEmitter.event()
                    .data(json);
            this.sseEmitter.send(event);
        } catch (IOException e) {
            this.sseEmitter.completeWithError(e);
        }
    }
}
