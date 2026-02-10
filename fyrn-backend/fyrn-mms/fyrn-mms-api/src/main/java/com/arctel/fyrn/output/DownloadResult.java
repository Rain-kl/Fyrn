package com.arctel.fyrn.output;

import lombok.Getter;

import java.util.Map;

/**
 * 文件下载结果封装类
 */
@Getter
public class DownloadResult {
    // Getter 方法
    private final byte[] fileData;
    private final Map<String, String> headers;
    private final String fileName;

    // 私有构造函数
    private DownloadResult( byte[] fileData, Map<String, String> headers,
                           String fileName) {
        this.fileData = fileData;
        this.headers = headers;
        this.fileName = fileName;
    }

    /**
     * 成功下载的结果
     */
    public static DownloadResult success(byte[] fileData, Map<String, String> headers, String fileName) {
        return new DownloadResult(fileData, headers, fileName);
    }


}