package com.github.linyuzai.connection.loadbalance.autoconfigure.redisson;

import com.github.linyuzai.connection.loadbalance.core.subscribe.MasterSlaveConnectionSubscriber;
import com.github.linyuzai.connection.loadbalance.core.subscribe.MasterSlaveConnectionSubscriberFactory;
import lombok.Getter;
import lombok.Setter;
import org.redisson.api.RedissonClient;

@Getter
@Setter
public class RedissonTopicConnectionSubscriberFactory extends MasterSlaveConnectionSubscriberFactory {

    private RedissonClient redissonClient;

    private boolean shared;

    @Override
    public MasterSlaveConnectionSubscriber doCreate(String scope) {
        return new RedissonTopicConnectionSubscriber(redissonClient, shared);
    }
}
