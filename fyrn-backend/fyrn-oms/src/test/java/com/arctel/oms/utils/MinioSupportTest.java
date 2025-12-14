package com.arctel.oms.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
@Slf4j
class MinioSupportTest {

    @Resource
    MinioSupport minioSupport;

    @Test
    void upload() {
        String randomContent = "This is a test file for MinIO upload.";
        byte[] contentBytes = randomContent.getBytes();
        String objectName = "test-folder/test-file.txt";
        String upload = minioSupport.upload(contentBytes, objectName);
        log.info(upload);
    }
}