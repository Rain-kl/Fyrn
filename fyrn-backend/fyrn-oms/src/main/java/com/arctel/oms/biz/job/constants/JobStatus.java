package com.arctel.oms.biz.job.constants;

import lombok.Getter;

@Getter
public enum JobStatus {
    PENDING("PENDING", "等待中"),
    RUNNING("RUNNING", "运行中"),
    SUCCESS("SUCCESS", "成功"),
    FAILED("FAILED", "失败"),
    CANCELLED("CANCELLED", "已取消");

    private final String code;
    private final String description;

    JobStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 判断当前状态是否为终态（即任务不会再发生变化）
     */
    public boolean isTerminal() {
        return this == SUCCESS || this == FAILED || this == CANCELLED;
    }

    /**
     * 根据 code 获取对应的 JobStatus 枚举
     *
     * @param code 状态码
     * @return 对应的枚举，若未找到则返回 null
     */
    public static JobStatus fromCode(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        for (JobStatus status : JobStatus.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断给定状态是否有效（即是否属于已定义的枚举值）
     */
    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }
}