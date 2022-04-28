package com.github.linyuzai.connection.loadbalance.core.message;

import com.github.linyuzai.connection.loadbalance.core.concept.Connection;
import com.github.linyuzai.connection.loadbalance.core.event.ConnectionEventListener;
import com.github.linyuzai.connection.loadbalance.core.event.MessageReceiveEvent;

import java.sql.Types;

public interface MessageHandler extends ConnectionEventListener {

    @Override
    default void onEvent(Object event) {
        if (event instanceof MessageReceiveEvent) {
            Message message = ((MessageReceiveEvent) event).getMessage();
            Connection connection = ((MessageReceiveEvent) event).getConnection();
            if (Connection.Type.CLIENT.equals(connection.getType())) {
                onMessage(message, connection);
            }
        }
    }

    void onMessage(Message message, Connection connection);
}
