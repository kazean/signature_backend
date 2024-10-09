package org.delivery.storeadmin.domain.sse.connection.ifs;

import lombok.extern.slf4j.Slf4j;
import org.delivery.storeadmin.domain.sse.connection.model.UserSseConnection;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SseConnectionPool implements ConnectionPoolIfs<String, UserSseConnection> {
    private final Map<String, UserSseConnection> connectionPool = new ConcurrentHashMap<>();

    @Override
    public void addSession(String key, UserSseConnection userSseConnection) {
        connectionPool.put(key, userSseConnection);
    }

    @Override
    public UserSseConnection getSession(String uniqueKey) {
        return connectionPool.get(uniqueKey);
    }

    @Override
    public void conCompletionCallback(UserSseConnection session) {
        log.info("callback connection pool completion : {}", session);
        connectionPool.remove(session.getUniqueKey());
    }
}
