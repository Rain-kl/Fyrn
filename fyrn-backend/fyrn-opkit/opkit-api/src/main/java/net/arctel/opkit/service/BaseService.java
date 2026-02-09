package net.arctel.opkit.service;

public interface BaseService {

    /**
     * 翻译文本
     *
     * @param taskId 任务ID
     * @return 翻译后的文本
     */
    <R> R queryResponse(String taskId, Class<R> clazz);


}
