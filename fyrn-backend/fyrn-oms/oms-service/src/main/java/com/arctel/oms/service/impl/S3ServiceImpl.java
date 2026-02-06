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

package com.arctel.oms.service.impl;

import com.arctel.oms.common.exception.BizException;
import com.arctel.oms.infrastructure.config.OssProperties;
import com.arctel.oms.service.OmsStorageService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.BucketAlreadyExistsException;
import software.amazon.awssdk.services.s3.model.BucketAlreadyOwnedByYouException;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Slf4j
@Service
public class S3ServiceImpl implements OmsStorageService {

    @Resource
    private OssProperties ossProperties;

    private S3Client s3Client;

    @PostConstruct
    public void init() {
        S3ClientBuilder s3ClientBuilder = S3Client.builder()
                .region(Region.of(ossProperties.getRegion()))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        ossProperties.getAccessKeyId(),
                                        ossProperties.getAccessKeySecret())));
        if (StringUtils.isNotBlank(ossProperties.getEndpoint())) {
            this.s3Client = s3ClientBuilder
                    // 如果是私有 S3 / 兼容 S3 的存储（比如 COS / OSS / MinIO）
                    .endpointOverride(URI.create(ossProperties.getEndpoint()))
                    // MinIO 需要使用 path-style 访问模式
                    .forcePathStyle(true)
                    .build();
        } else {
            this.s3Client = s3ClientBuilder.build();
        }
        log.info("S3 client initialized, region={}", ossProperties.getRegion());

        // 确保 bucket 存在
        ensureBucketExists();
    }

    /**
     * 确保 bucket 存在，如果不存在则创建
     */
    private void ensureBucketExists() {
        String bucketName = ossProperties.getBucketName();
        try {
            // 检查 bucket 是否存在
            s3Client.headBucket(HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build());
            log.info("Bucket check pass: {}", bucketName);
        } catch (NoSuchBucketException e) {
            // Bucket 不存在，创建它
            try {
                s3Client.createBucket(CreateBucketRequest.builder()
                        .bucket(bucketName)
                        .build());
                log.info("Bucket created successfully: {}", bucketName);
            } catch (BucketAlreadyExistsException | BucketAlreadyOwnedByYouException ex) {
                log.info("Bucket already exists (race condition): {}", bucketName);
            } catch (Exception ex) {
                log.error("Failed to create bucket: {}", bucketName, ex);
                throw new BizException("Failed to create bucket: " + ex.getMessage());
            }
        } catch (Exception e) {
            log.error("Failed to check bucket existence: {}", bucketName, e);
            throw new BizException("Failed to check bucket: " + e.getMessage());
        }
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
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectName)
                    .contentType(
                            contentType == null
                                    ? "application/octet-stream"
                                    : contentType)
                    .contentLength((long) bytes.length)
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(bytes));

        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new BizException("文件上传失败:" + e.getMessage());
        }

        String path = "/" + bucketName + "/" + objectName;
        log.info("文件上传到: {}", path);
        return path;
    }

    /**
     * 下载文件（流式）
     * 返回 ResponseInputStream，调用方必须 close
     */
    @Override
    public InputStream downloadStream(String objectName) {
        String bucketName = ossProperties.getBucketName();

        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectName)
                    .build();

            // ResponseInputStream<GetObjectResponse> extends InputStream
            return s3Client.getObject(request);

        } catch (Exception e) {
            log.error("文件下载失败: {}", e.getMessage(), e);
            throw new BizException("文件下载失败:" + e.getMessage());
        }
    }

    /**
     * 下载为 byte[]
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
     * 下载并保存到本地
     */
    @Override
    public Path downloadToFile(String objectName, Path targetFile) {
        try {
            Path parent = targetFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (InputStream in = downloadStream(objectName);
                    OutputStream out = Files.newOutputStream(
                            targetFile,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING,
                            StandardOpenOption.WRITE)) {

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
     * 通用 copy
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