package net.arctel.framework.core.task.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TaskQueueConfig {

    /**
     * Stream 读取位置，初始为 $ (只消费新消息)，也可以配置为 0-0 (从头开始消费)
     */
    public static final String LAST_READ_NEW_MESSAGES = "$";
    public static final String LAST_READ_FROM_BEGINNING = "0-0";

    /**
     * 任务队列容量
     */
    private Long maxQueueSize;
    /**
     * 心跳保持间隔时间，单位秒
     */
    private Long keepAliveInterval;
    /**
     * Stream 阻塞读取超时时间，单位秒
     */
    private Long blockTimeoutSeconds;
    /**
     * 每次读取的批量大小
     */
    private Long batchSize;

    /**
     * Stream 读取位置，初始为 $ (只消费新消息)，可以通过配置覆盖默认值
     */
    private String lastReadId;

    /**
     * 启动时自动创建消费组
     */
    private boolean createGroupOnStart;

    public TaskQueueConfig() {
        this.maxQueueSize = 100L;
        this.keepAliveInterval = 5L;
        this.blockTimeoutSeconds = 5L;
        this.batchSize = 10L;
        this.lastReadId = LAST_READ_FROM_BEGINNING;
        this.createGroupOnStart = true;
    }
}