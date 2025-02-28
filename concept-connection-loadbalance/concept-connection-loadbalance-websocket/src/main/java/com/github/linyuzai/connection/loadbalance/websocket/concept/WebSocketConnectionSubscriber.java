package com.github.linyuzai.connection.loadbalance.websocket.concept;

import com.github.linyuzai.connection.loadbalance.core.concept.ConnectionLoadBalanceConcept;
import com.github.linyuzai.connection.loadbalance.core.server.ConnectionServer;
import com.github.linyuzai.connection.loadbalance.core.subscribe.ServerInstanceConnectionSubscriber;
import lombok.Getter;
import lombok.Setter;

import java.net.URI;
import java.util.function.Consumer;

/**
 * ws 连接订阅者。
 * <p>
 * ws connection subscriber.
 */
@Getter
@Setter
public abstract class WebSocketConnectionSubscriber<T extends WebSocketConnection>
        extends ServerInstanceConnectionSubscriber<T> {

    private String protocol = "ws";

    @Override
    public void doSubscribe(ConnectionServer server, ConnectionLoadBalanceConcept concept,
                            Consumer<T> onSuccess, Consumer<Throwable> onError,
                            Runnable onComplete) {
        URI uri = getUri(server);
        doSubscribe(uri, concept, connection -> {
            connection.getMetadata().put(ConnectionServer.class, server);
            onSuccess.accept(connection);
        }, onError, onComplete);
    }

    public abstract void doSubscribe(URI uri, ConnectionLoadBalanceConcept concept,
                                     Consumer<T> onSuccess,
                                     Consumer<Throwable> onError,
                                     Runnable onComplete);

    @Override
    public String getEndpoint() {
        return WebSocketLoadBalanceConcept.SUBSCRIBER_ENDPOINT;
    }

    public abstract String getType();
}
