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

package com.arctel.biz.impl;

import com.arctel.biz.file.OosService;
import com.arctel.pub.config.OssProperties;
import com.arctel.pub.exception.BizException;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Slf4j
@Service
public class MinioServiceImpl implements OosService {

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
     * 文件上传（byte[]）
     */
    @Override
    public String upload(byte[] bytes, String objectName) {
        return upload(bytes, objectName, "application/octet-stream");
    }

    /**
     * 文件上传（byte[] + contentType）
     */
    @Override
    public String upload(byte[] bytes, String objectName, String contentType) {
        String bucketName = ossProperties.getBucketName();

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(new ByteArrayInputStream(bytes), bytes.length, -1)
                            .contentType(contentType == null ? "application/octet-stream" : contentType)
                            .build()
            );
        } catch (Exception e) {
            log.error("文件上传失败:{}", e.getMessage(), e);
            throw new BizException("文件上传失败:" + e.getMessage());
        }

        String path = "/" + bucketName + objectName;
        log.info("文件上传到:{}", path);
        return path;
    }

    /**
     * 下载文件（流式传输）
     * <p>
     * 返回 InputStream（实际类型 GetObjectResponse），调用方用完必须 close。
     */
    @Override
    public InputStream downloadStream(String objectName) {
        String bucketName = ossProperties.getBucketName();
        try {
            // GetObjectResponse extends InputStream
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            log.error("文件下载失败: {}", e.getMessage(), e);
            throw new BizException("文件下载失败:" + e.getMessage());
        }
    }

    /**
     * 辅助：下载并返回 byte[]
     * 注意：大文件不建议用此方法（会占用大量内存），优先用 downloadStream()。
     */
    @Override
    public byte[] downloadBytes(String objectName) {
        try (InputStream in = downloadStream(objectName);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            copy(in, out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("文件下载为字节数组失败: {}", e.getMessage(), e);
            throw new BizException("文件下载为字节数组失败:" + e.getMessage());
        }
    }

    /**
     * 辅助：下载并保存到本地文件
     *
     * @return 保存后的目标路径
     */
    @Override
    public Path downloadToFile(String objectName, Path targetFile) {
        try {
            // 确保父目录存在
            Path parent = targetFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (InputStream in = downloadStream(objectName);
                 OutputStream out = Files.newOutputStream(
                         targetFile,
                         StandardOpenOption.CREATE,
                         StandardOpenOption.TRUNCATE_EXISTING,
                         StandardOpenOption.WRITE
                 )) {
                copy(in, out);
            }

            log.info("文件已保存到本地: {}", targetFile.toAbsolutePath());
            return targetFile;
        } catch (Exception e) {
            log.error("文件下载并保存本地失败: {}", e.getMessage(), e);
            throw new BizException("文件下载并保存本地失败:" + e.getMessage());
        }
    }

    /**
     * 通用 copy（8KB buffer）
     */
    private static long copy(InputStream in, OutputStream out) throws Exception {
        byte[] buffer = new byte[8192];
        long total = 0;
        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
            total += len;
        }
        out.flush();
        return total;
    }
}