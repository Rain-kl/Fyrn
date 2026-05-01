/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.arctel.opkit.service.impl;


import jakarta.annotation.Resource;
import net.arctel.platform.framework.task.utils.TaskRedisUtil;
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
