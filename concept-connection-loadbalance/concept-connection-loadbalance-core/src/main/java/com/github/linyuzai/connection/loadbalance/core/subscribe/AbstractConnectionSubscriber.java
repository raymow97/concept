package com.github.linyuzai.connection.loadbalance.core.subscribe;

import com.github.linyuzai.connection.loadbalance.core.concept.Connection;
import com.github.linyuzai.connection.loadbalance.core.concept.ConnectionLoadBalanceConcept;
import com.github.linyuzai.connection.loadbalance.core.message.*;
import com.github.linyuzai.connection.loadbalance.core.server.ConnectionServer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class AbstractConnectionSubscriber implements ConnectionSubscriber {

    public static final String DELIMITER = "_";

    public static final String PREFIX = "LBConnection";

    @Override
    public void subscribe(Consumer<Connection> onSuccess,
                          Consumer<Throwable> onError,
                          Runnable onComplete,
                          ConnectionLoadBalanceConcept concept) {
        ConnectionServer local = concept.getConnectionServerManager().getLocal();
        //单体应用不需要转发
        if (local == null) {
            onComplete.run();
            return;
        }
        ConnectionServer server = getSubscribeServer();
        try {
            String topic = getTopic(concept);
            String from = getFrom(concept);
            String id = getId(topic, from, server);

            //subscriber
            Connection existSubscriber = concept.getConnectionRepository()
                    .get(id, Connection.Type.SUBSCRIBER);
            if (existSubscriber != null) {
                if (existSubscriber.isAlive()) {
                    //如果连接还存活则直接返回
                    return;
                } else {
                    //否则关闭连接
                    existSubscriber.close("NotAlive");
                }
            }

            Map<Object, Object> context = new LinkedHashMap<>();

            Connection subscriber = createSubscriber(id, topic, context, concept);
            if (subscriber != null) {
                subscriber.getMetadata().put(ConnectionServer.class, server);
                onSuccess.accept(subscriber);
            }

            //observable
            Connection existObservable = concept.getConnectionRepository()
                    .get(id, Connection.Type.OBSERVABLE);
            if (existObservable != null) {
                if (existObservable.isAlive()) {
                    //如果连接还存活则直接返回
                    return;
                } else {
                    //否则关闭连接
                    existObservable.close("NotAlive");
                }
            }
            Connection observable = createObservable(id, topic, context, concept);
            if (observable != null) {
                onSuccess.accept(observable);
            }
        } catch (Throwable e) {
            onError.accept(new ConnectionServerSubscribeException(server, e.getMessage(), e));
        } finally {
            onComplete.run();
        }
    }

    /**
     * LBConnection_[websocket/netty]_${serviceId}_${host:port}_[redisson/redis/rabbit/kakfa]
     */
    protected String getId(String topic, String from, ConnectionServer subscribe) {
        return topic + DELIMITER + from + DELIMITER + subscribe.getServiceId();
    }

    protected String getFrom(ConnectionLoadBalanceConcept concept) {
        return ConnectionServer.url(concept.getConnectionServerManager().getLocal());
    }

    /**
     * LBConnection_[websocket/netty]_${serviceId}
     */
    protected String getTopic(ConnectionLoadBalanceConcept concept) {
        ConnectionServer local = concept.getConnectionServerManager().getLocal();
        return PREFIX + DELIMITER + concept.getId() + DELIMITER + local.getServiceId();
    }

    protected void onMessageReceived(Connection connection, Object message) {
        connection.getConcept().onMessage(connection, message, msg -> {
            ConnectionLoadBalanceConcept concept = connection.getConcept();
            return !Objects.equals(getFrom(concept), msg.getFrom()) &&
                    getMessageIdempotentVerifier(concept).verify(msg);
        });
    }

    protected MessageIdempotentVerifier getMessageIdempotentVerifier(ConnectionLoadBalanceConcept concept) {
        return concept.getMessageIdempotentVerifier();
    }

    protected abstract Connection createSubscriber(String id,
                                                   String topic,
                                                   Map<Object, Object> context,
                                                   ConnectionLoadBalanceConcept concept);

    protected abstract Connection createObservable(String id,
                                                   String topic,
                                                   Map<Object, Object> context,
                                                   ConnectionLoadBalanceConcept concept);

    protected abstract ConnectionServer getSubscribeServer();
}
