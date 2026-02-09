package com.arctel.oms.infrastructure.task.base;

import com.alibaba.fastjson2.JSON;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

import static com.arctel.oms.common.constants.RedisPrefixConstant.TASK_VALUE_PREFIX;

public class TaskRspCollectorUtil {


    /**
     * 收集任务响应结果，存储到Redis中，过期时间7天
     */
    public static <T extends BaseTaskMessage> void collectResponse(RedisTemplate<String, Object> redisTemplate, T taskMessage, Object response) {
        String redisKey = TASK_VALUE_PREFIX + taskMessage.getTaskId();
        redisTemplate.opsForValue().set(redisKey, response);
        redisTemplate.expire(redisKey, 7, TimeUnit.DAYS);
    }

    public static Object getResponse(RedisTemplate<String, Object> redisTemplate, String taskId) {
        //TODO: 待改造为stream，目前先简单的轮询获取结果，等待1秒后重试，最多重试3次
        String redisKey = TASK_VALUE_PREFIX + taskId;
        for (int i = 0; i < 3; i++) {
            Object o = redisTemplate.opsForValue().get(redisKey);
            if (o != null) {
                return o;
            }
            try {
                // 如果没有获取到结果，等待1秒后重试，最多重试3次
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return null;
    }

    public static <R> R getResponse(RedisTemplate<String, Object> redisTemplate, String taskId, Class<R> clazz) {
        Object response = TaskRspCollectorUtil.getResponse(redisTemplate, taskId);
        if (response == null) {
            return null;
        }
        return JSON.parseObject(JSON.toJSONString(response), clazz);
    }

}
