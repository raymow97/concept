package com.github.linyuzai.connection.loadbalance.websocket.reactive;

import com.github.linyuzai.connection.loadbalance.core.concept.Connection;
import com.github.linyuzai.connection.loadbalance.websocket.concept.WebSocketLoadBalanceConcept;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 基于 {@link ReactiveWebSocketConnection} 默认服务的 {@link WebSocketHandler}。
 * <p>
 * {@link WebSocketHandler} based on {@link ReactiveWebSocketConnection} for default service.
 */
@AllArgsConstructor
public class ReactiveWebSocketServerHandler implements WebSocketHandler {

    private WebSocketLoadBalanceConcept concept;

    @NonNull
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        Mono<Void> send = session.send(Flux.create(sink ->
                concept.onEstablish(new Object[]{session, sink}, null)));

        Mono<Void> receive = session.receive()
                .doOnNext(it -> concept.onMessage(session.getId(), Connection.Type.CLIENT, it))
                .doOnError(it -> concept.onError(session.getId(), Connection.Type.CLIENT, it))
                .then();

        @SuppressWarnings("all")
        Disposable disposable = session.closeStatus()
                .doOnError(it -> concept.onError(session.getId(), Connection.Type.CLIENT, it))
                .subscribe(it -> concept.onClose(session.getId(), Connection.Type.CLIENT, it));

        return Mono.zip(send, receive).then();
    }
}
