package org.delivery.storeadmin.domain.sse.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.delivery.storeadmin.domain.authorization.model.UserSession;
import org.delivery.storeadmin.domain.sse.connection.ifs.SseConnectionPool;
import org.delivery.storeadmin.domain.sse.connection.model.UserSseConnection;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/sse")
public class SseApiController {
    private static final Map<String, SseEmitter> userConnection = new ConcurrentHashMap<>();
    private final SseConnectionPool sseConnectionPool;
    private final ObjectMapper objectMapper;

    @GetMapping(path = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseBodyEmitter connect(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserSession userSession
    ) {
        log.info("login user: {}", userSession);
        var userSessionConnection = UserSseConnection.connect(
                userSession.getStoreId().toString(),
                sseConnectionPool,
                objectMapper
        );
        sseConnectionPool.addSession(userSessionConnection.getUniqueKey(), userSessionConnection);
        return userSessionConnection.getSseEmitter();
        /*SseEmitter emitter = new SseEmitter(1000L * 60);
        userConnection.put(userSession.getUserId().toString(), emitter);

        emitter.onTimeout(() -> {
            log.info("on timeout");
            emitter.complete();
        });

        emitter.onCompletion(() -> {
            log.info("on completion");
            userConnection.remove(userSession.getUserId().toString());
        });

        SseEmitter.SseEventBuilder event = SseEmitter.event()
                .name("onopen");

        try {
            emitter.send(event);
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
        return emitter;*/
    }

    @GetMapping("/push-event")
    public void pushEvent(@Parameter(hidden = true)
        @AuthenticationPrincipal UserSession userSession
    ){
        var userSessionConnection = sseConnectionPool.getSession(userSession.getStoreId().toString());
        Optional.ofNullable(userSessionConnection)
                .ifPresent(it -> {
                    it.sendMessage("hello world");
                });
        /*SseEmitter emitter = userConnection.get(userSession.getUserId().toString());
        SseEmitter.SseEventBuilder event = SseEmitter.event()
                .data("hello");

        try {
            emitter.send(event);
        } catch (IOException e) {
            emitter.completeWithError(e);
        }*/
    }
}
