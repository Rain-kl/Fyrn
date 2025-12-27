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

package com.arctel.oms.support;

import com.arctel.oms.biz.file.OosService;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.file.Path;

@Slf4j
@Component
public class MinioSupport {


    @Resource
    OosService oosService;

    /**
     * 文件上传（byte[]）
     */
    public String upload(byte[] bytes, String objectName) {
        return upload(bytes, objectName, "application/octet-stream");
    }

    /**
     * 文件上传（byte[] + contentType）
     */
    public String upload(byte[] bytes, String objectName, String contentType) {
        return oosService.upload(bytes, objectName, contentType);
    }

    /**
     * 下载文件（流式传输）
     * <p>
     * 返回 InputStream（实际类型 GetObjectResponse），调用方用完必须 close。
     */
    public InputStream downloadStream(String objectName) {
        return oosService.downloadStream(objectName);
    }

    /**
     * 辅助：下载并返回 byte[]
     * 注意：大文件不建议用此方法（会占用大量内存），优先用 downloadStream()。
     */
    public byte[] downloadBytes(String objectName) {
        return oosService.downloadBytes(objectName);
    }

    /**
     * 辅助：下载并保存到本地文件
     *
     * @return 保存后的目标路径
     */
    public Path downloadToFile(String objectName, Path targetFile) {
        return oosService.downloadToFile(objectName, targetFile);
    }

}