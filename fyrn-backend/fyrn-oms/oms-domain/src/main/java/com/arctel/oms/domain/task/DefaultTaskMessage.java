package com.arctel.oms.domain.task;

import lombok.Data;

import java.util.Date;

@Data
public class DefaultTaskMessage extends BaseTaskMessage {

    /**
     * 业务标签
     */
    private String bizTag;

    /**
     * 业务值
     */
    private String bizValue;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;


}
