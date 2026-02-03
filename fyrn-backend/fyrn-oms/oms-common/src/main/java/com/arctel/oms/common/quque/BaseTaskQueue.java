package com.arctel.oms.common.quque;

import com.arctel.oms.domain.dto.BaseTaskMessageDTO;
import com.arctel.oms.domain.dto.RegistrationInfoDTO;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.RedisTemplate;

public abstract class BaseTaskQueue<T extends BaseTaskMessageDTO> implements InitializingBean {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private Class<T> taskMsgClass;

    public static final String TASK_QUEUE_PREFIX = "TASK_GROUP:";

    private String queueKey;
    private String tasksQueueKey;

    /**
     * 获取注册信息, 将任务队列注册到系统中
     */
    public abstract RegistrationInfoDTO<T> getRegistrationInfo();

    @Override
    public void afterPropertiesSet() throws Exception {
        initTaskQueue();
    }

    public void initTaskQueue() {
        register();
    }

    public void register() {
        RegistrationInfoDTO<T> registrationInfoDTO = getRegistrationInfo();
        this.queueKey = TASK_QUEUE_PREFIX + registrationInfoDTO.getQueueName();
        this.tasksQueueKey = queueKey + ":tasks";
        this.taskMsgClass = registrationInfoDTO.getClazz();

        // 在 Redis 中注册任务队列信息
        redisTemplate.opsForHash().put(queueKey, "description", registrationInfoDTO.getDescription());
    }

    /**
     * 添加任务信息
     *
     * @param taskMsg 任务信息
     */
    public Boolean put(T taskMsg) {
        redisTemplate.opsForList().rightPush(tasksQueueKey, taskMsg);
        return null;
    }

    /**
     * 获取任务信息
     */
    public T pop() {
        Object o = redisTemplate.opsForList().rightPop(tasksQueueKey);
        return taskMsgClass.cast(o);
    }

    /**
     * 自动消费任务
     */
    void autoConsumption() {

    }

}
