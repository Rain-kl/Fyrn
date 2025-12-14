package com.arctel.oms.utils;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest()
class MinioSupportTest {

    @Resource
    MinioSupport minioSupport;

    @Test
    void upload() {
        String randomContent = "This is a test file for MinIO upload.";
        byte[] contentBytes = randomContent.getBytes();
        String objectName = "test-folder/test-file.txt";
        minioSupport.upload(contentBytes, objectName);
    }
}