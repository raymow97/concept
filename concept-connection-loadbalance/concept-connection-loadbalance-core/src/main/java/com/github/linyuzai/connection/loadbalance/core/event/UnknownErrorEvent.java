package com.github.linyuzai.connection.loadbalance.core.event;

import com.github.linyuzai.connection.loadbalance.core.concept.Connection;
import com.github.linyuzai.connection.loadbalance.core.concept.ConnectionLoadBalanceConcept;
import com.github.linyuzai.connection.loadbalance.core.extension.IdConnection;
import lombok.Getter;

@Getter
public class UnknownErrorEvent implements ConnectionEvent, ErrorEvent {

    private final Connection connection;

    private final Throwable error;

    public UnknownErrorEvent(Object id, String type, Throwable e, ConnectionLoadBalanceConcept concept) {
        this.connection = new IdConnection(id, type, concept);
        this.error = e;
    }
}
