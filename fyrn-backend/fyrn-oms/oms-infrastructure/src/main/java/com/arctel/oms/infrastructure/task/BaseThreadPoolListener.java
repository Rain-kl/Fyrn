package com.arctel.oms.infrastructure.task;

public abstract class BaseThreadPoolListener<T> {


    public abstract Boolean initConfig();

    /**
     * 执行具体的任务逻辑
     * @param taskMsg 任务消息, 根据消息调度具体的业务处理
     * @return 任务执行结果，true表示成功，false表示失败
     */
    public abstract Boolean doExecute(T taskMsg);

}
