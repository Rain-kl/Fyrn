package com.arctel.domain.dto;


import lombok.Data;

@Data
public class LocalFileSimpleDTO {
    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件大小，单位：字节
     */
    private Long fileSize;

    /**
     * 小说字数, 单位：字
     */
    private Long wordCount;

}
