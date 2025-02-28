package com.github.linyuzai.connection.loadbalance.core.message.idempotent;

import com.github.linyuzai.connection.loadbalance.core.concept.ConnectionLoadBalanceConcept;
import com.github.linyuzai.connection.loadbalance.core.message.Message;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 消息幂等校验器实现。
 * <p>
 * Verify idempotent of message in memory.
 */
public class InMemoryMessageIdempotentVerifier implements MessageIdempotentVerifier {

    private final Map<String, Boolean> ids = new ConcurrentHashMap<>();

    @Override
    public boolean verify(Message message, ConnectionLoadBalanceConcept concept) {
        String id = message.getId();
        if (id == null) {
            return true;
        }
        Holder holder = new Holder();
        ids.computeIfAbsent(id, k -> {
            //如果不存在则会回调进入，说明消息没有消费过，并添加这个消息ID（同步的）
            //If message id is not duplicate and put this message id in sync
            holder.verified = true;
            return Boolean.TRUE;
        });
        return holder.verified;
    }

    static class Holder {

        boolean verified = false;
    }
}
