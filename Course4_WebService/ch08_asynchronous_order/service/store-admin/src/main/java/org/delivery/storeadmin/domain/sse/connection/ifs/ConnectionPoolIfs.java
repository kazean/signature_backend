package org.delivery.storeadmin.domain.sse.connection.ifs;

public interface ConnectionPoolIfs<T, R> {
    void addSession(T key, R session);

    R getSession(T uniqueKey);

    void conCompletionCallback(R session);
}
