package com.arctel.oms.infrastructure.task.base;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;

public abstract class BaseTaskRspCollector<T extends BaseTaskMessage> {

    @Resource
    public RedisTemplate<String, Object> redisTemplate;

    protected abstract T getTaskMessage();

    /**
     * 收集任务响应结果，存储到Redis中，过期时间7天
     */
    public void collectResponse(Object response) {
        T taskMessage = getTaskMessage();
        TaskRspCollectorUtil.collectResponse(redisTemplate, taskMessage, response);
    }

    public Object getResponse() {
        T taskMessage = getTaskMessage();
        return TaskRspCollectorUtil.getResponse(redisTemplate, taskMessage.getTaskId());
    }

    public <R> R getResponse(Class<R> clazz) {
        T taskMessage = getTaskMessage();
        return TaskRspCollectorUtil.getResponse(redisTemplate, taskMessage.getTaskId(), clazz);
    }

}
