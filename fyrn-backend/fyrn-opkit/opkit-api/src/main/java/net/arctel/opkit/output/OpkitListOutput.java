package net.arctel.opkit.output;

import lombok.Data;

import java.util.List;

@Data
public class OpkitListOutput {

    /**
     * 工具名称
     */
    String toolName;

    /**
     * 工具描述
     */
    String description;

    /**
     * 工具处理器标签
     */
    String handlerTag;

    /**
     * 是否可用
     */
    Boolean available;

    /**
     * 工具版本
     */
    String version;


}
