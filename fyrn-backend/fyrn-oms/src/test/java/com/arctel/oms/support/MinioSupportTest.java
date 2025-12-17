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

import com.arctel.oms.config.OssProperties;
import com.arctel.oms.common.exception.BizException;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MinioSupportTest {

    @Mock
    private MinioClient minioClient;

    @TempDir
    Path tempDir;

    private MinioSupport minioSupport;

    @BeforeEach
    void setUp() {
        minioSupport = new MinioSupport();
        OssProperties ossProperties = new OssProperties();
        ossProperties.setEndpoint("http://localhost:9000");
        ossProperties.setBucketName("test-bucket");
        ossProperties.setAccessKeyId("eZkIBk8WaWlfxjCx5OAW");
        ossProperties.setAccessKeySecret("5smkTin7spqzPpfHH82UphdBS2YBhr1W3f1muzUT");
        ReflectionTestUtils.setField(minioSupport, "ossProperties", ossProperties);
        ReflectionTestUtils.setField(minioSupport, "minioClient", minioClient);
    }

    @Test
    void uploadShouldReturnAccessibleUrlAndInvokeMinio() throws Exception {
        byte[] payload = "payload".getBytes(StandardCharsets.UTF_8);
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(Mockito.mock(ObjectWriteResponse.class));

        String url = minioSupport.upload(payload, "dir/file.txt", "text/plain");

        assertEquals("/test-bucket/dir/file.txt", url);
        ArgumentCaptor<PutObjectArgs> captor = ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(minioClient).putObject(captor.capture());
        assertEquals("test-bucket", captor.getValue().bucket());
        assertEquals("dir/file.txt", captor.getValue().object());
    }

    @Test
    void uploadShouldWrapExceptionFromMinio() throws Exception {
        when(minioClient.putObject(any(PutObjectArgs.class))).thenThrow(new RuntimeException("boom"));

        BizException ex = assertThrows(BizException.class,
                () -> minioSupport.upload("x".getBytes(StandardCharsets.UTF_8), "file.txt"));

        assertTrue(ex.getMessage().contains("文件上传失败"));
    }

    @Test
    void downloadBytesShouldReturnExactContent() throws Exception {
        MinioSupport spySupport = Mockito.spy(minioSupport);
        byte[] expected = "hello".getBytes(StandardCharsets.UTF_8);
        Mockito.doReturn(new ByteArrayInputStream(expected))
                .when(spySupport).downloadStream("object.txt");

        byte[] actual = spySupport.downloadBytes("object.txt");

        assertArrayEquals(expected, actual);
    }

    @Test
    void downloadToFileShouldPersistContent() throws Exception {
        MinioSupport spySupport = Mockito.spy(minioSupport);
        byte[] expected = "persist me".getBytes(StandardCharsets.UTF_8);

        Mockito.doReturn(new ByteArrayInputStream(expected))
                .when(spySupport).downloadStream("persist.bin");

        Path target = tempDir.resolve("nested/persist.bin");
        Path result = spySupport.downloadToFile("persist.bin", target);

        assertTrue(Files.exists(result));
        assertArrayEquals(expected, Files.readAllBytes(result));
    }
}
