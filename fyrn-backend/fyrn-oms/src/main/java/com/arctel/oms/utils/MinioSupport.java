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

package com.arctel.oms.utils;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;


@Slf4j
@Component
public class MinioSupport {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access-key-id}")
    private String accessKey;

    @Value("${minio.access-key-secret}")
    private String secretKey;

    @Value("${minio.bucket-name}")
    private String bucketName;

    private MinioClient minioClient;

    @PostConstruct
    public void init() {
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        log.info("MinIO client initialized with endpoint: {}", endpoint);
    }


    /**
     * 文件上传
     *
     * @param bytes
     * @param objectName
     * @return
     */
    public String upload(byte[] bytes, String objectName) {

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
        } catch (MinioException e) {
            log.error("MinIO异常: {}", e.getMessage());
            System.out.println("MinIO异常: " + e.getMessage());
            throw new RuntimeException("文件上传失败", e);
        } catch (IOException e) {
            log.error("IO异常: {}", e.getMessage());
            System.out.println("IO异常: " + e.getMessage());
            throw new RuntimeException("文件上传失败", e);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            log.error("安全异常: {}", e.getMessage());
            System.out.println("安全异常: " + e.getMessage());
            throw new RuntimeException("文件上传失败", e);
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