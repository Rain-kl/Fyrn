package net.arctel.framework.core.task.utils;

import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

import java.util.HashSet;
import java.util.Set;

import static net.arctel.framework.constants.RedisPrefixConstant.TASK_METRICS_HANDLER_PREFIX;
import static net.arctel.framework.constants.RedisPrefixConstant.TASK_METRICS_QUEUE_PREFIX;

public class TaskRedisUtil {

    public static Set<String> scanKeys(RedisTemplate<String, Object> redisTemplate, String pattern) {
        return redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keys = new HashSet<>();
            ScanOptions options = ScanOptions.scanOptions()
                    .match(pattern)
                    .count(1000)
                    .build();

            Cursor<byte[]> cursor = connection.scan(options);
            while (cursor.hasNext()) {
                keys.add(new String(cursor.next()));
            }
            return keys;
        });
    }

    public static Set<String> getQueueKeys(RedisTemplate<String, Object> redisTemplate) {
        return scanKeys(redisTemplate, TASK_METRICS_QUEUE_PREFIX + "*");
    }

    public static Set<String> getHandlers(RedisTemplate<String, Object> redisTemplate) {
        Set<String> handlerKeys = scanKeys(redisTemplate, TASK_METRICS_HANDLER_PREFIX + "*");

        Set<String> handlers = new HashSet<>();
        handlerKeys.forEach(handlerKey -> {
            @SuppressWarnings("unchecked")
            Set<String> handlerSet = (Set<String>) redisTemplate.opsForValue().get(handlerKey);
            if (handlerSet != null) {
                handlers.addAll(handlerSet);
            }
        });
        return handlers;
    }
}
