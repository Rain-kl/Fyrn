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

package net.arctel.framework.core.task.fast;

import net.arctel.framework.constants.LogConstant;
import net.arctel.framework.core.task.model.BaseTaskMessage;
import net.arctel.framework.core.task.BaseTaskRspCollector;
import net.arctel.framework.core.task.TaskLogger;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Date;

import static net.arctel.framework.constants.RedisPrefixConstant.TASK_LOG_KEY_PREFIX;


/**
 * 基于Redis的任务日志记录器基类
 */
@Slf4j
public abstract class FastTaskLogger<T extends BaseTaskMessage> extends BaseTaskRspCollector<T> implements TaskLogger {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 获取任务消息（模板方法）
     */
    protected abstract T getTaskMessage();


    /**
     * 获取日志保留天数（钩子方法）
     */
    protected int getRetentionDays() {
        return 7;
    }

    @Override
    public final void logInfo(String logMessage) {
        executeWithContext(LogConstant.INFO, logMessage);
    }

    @Override
    public final void logError(String logMessage, Exception e) {
        log.error(logMessage, e);
        executeWithContext(LogConstant.ERROR, logMessage);
    }


    private void executeWithContext(String level, String message) {
        try {
            String key = TASK_LOG_KEY_PREFIX + getTaskMessage().getTaskId();
            // 追加到头部（最新在前）
            String formatLog = "[" + level + "] [" + new Date() + "] " + message;
            redisTemplate.opsForList().leftPush(key, formatLog);
            // 只保留最近1万条
            redisTemplate.opsForList().trim(key, 0, 9999);
            // 保留7天
            redisTemplate.expire(key, Duration.ofDays(getRetentionDays()));

        } catch (Exception e) {
            logger.error("Failed to save log to Redis for task: {}",
                    getTaskMessage().getTaskId(), e);
        }
    }

}
