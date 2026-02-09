
package net.arctel.opkit.service.impl;

import com.arctel.oms.infrastructure.task.base.TaskRspCollectorUtil;
import jakarta.annotation.Resource;
import net.arctel.opkit.service.BaseService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class BaseServiceImpl implements BaseService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 阻塞式查询任务结果, 直到结果返回或超时
     * @param taskId 任务ID
     */
    @Override
    public <R> R queryResponse(String taskId, Class<R> clazz) {
        return TaskRspCollectorUtil.getResponse(redisTemplate, taskId, clazz);
    }
}
