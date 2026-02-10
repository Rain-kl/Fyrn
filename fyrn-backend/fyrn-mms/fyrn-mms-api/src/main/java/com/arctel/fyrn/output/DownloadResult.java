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
