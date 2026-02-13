package net.arctel.opkit.service.impl;


import jakarta.annotation.Resource;
import net.arctel.framework.core.task.utils.TaskRedisUtil;
import net.arctel.opkit.output.OpkitListOutput;
import net.arctel.opkit.service.BaseToolService;
import net.arctel.opkit.service.OpkitService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class OpkitServiceImpl implements OpkitService {

    @Resource
    List<BaseToolService> baseToolServices;

    @Resource
    RedisTemplate<String, Object> redisTemplate;

    /**
     * 列出所有工具
     */
    @Override
    public List<OpkitListOutput> listTools() {
        ArrayList<OpkitListOutput> opkitListOutputs = new ArrayList<>();
        baseToolServices.forEach(baseToolService -> {
            OpkitListOutput toolInfo = baseToolService.getToolInfo();
            Set<String> handlers = TaskRedisUtil.getHandlers(redisTemplate);
            toolInfo.setAvailable(handlers.contains(baseToolService.getToolInfo().getHandlerTag()));
            opkitListOutputs.add(toolInfo);
        });
        return opkitListOutputs;
    }
}
