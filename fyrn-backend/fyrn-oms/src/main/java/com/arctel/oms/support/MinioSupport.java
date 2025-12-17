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

import com.arctel.oms.common.exception.BizException;
import com.arctel.oms.biz.oos.OosSupport;
import com.arctel.oms.biz.oos.OssProperties;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;


@Slf4j
@Component
public class MinioSupport implements OosSupport {

    @Resource
    OssProperties ossProperties;

    private MinioClient minioClient;

    @PostConstruct
    public void init() {
        this.minioClient = MinioClient.builder()
                .endpoint(ossProperties.getEndpoint())
                .credentials(ossProperties.getAccessKeyId(), ossProperties.getAccessKeySecret())
                .build();
        log.info("MinIO client initialized with endpoint: {}", ossProperties.getEndpoint());
    }


    /**
     * 文件上传
     *
     * @param bytes
     * @param objectName
     * @return
     */
    public String upload(byte[] bytes, String objectName) {
        String endpoint = ossProperties.getEndpoint();
        String bucketName = ossProperties.getBucketName();

        try {
            // 上传文件
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(new ByteArrayInputStream(bytes), bytes.length, -1)
                            .contentType("application/octet-stream")
                            .build()
            );
        } catch (Exception e) {
            log.error("文件上传失败:{}", e.getMessage(), e);
            throw new BizException("文件上传失败:" + e.getMessage());
        }

        // 文件访问路径规则 http://endpoint/bucketName/objectName
        StringBuilder stringBuilder = new StringBuilder();

        // 处理endpoint，如果没有http://或https://前缀，默认添加http://
        if (!endpoint.startsWith("http://") && !endpoint.startsWith("https://")) {
            stringBuilder.append("http://");
        }

        stringBuilder
                .append(endpoint.replaceFirst("^https?://", ""))
                .append("/")
                .append(bucketName)
                .append("/")
                .append(objectName);

        log.info("文件上传到:{}", stringBuilder.toString());

        return stringBuilder.toString();
    }
}