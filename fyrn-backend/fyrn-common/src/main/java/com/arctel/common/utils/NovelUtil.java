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

package com.arctel.common.utils;

import com.arctel.oms.utils.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 小说文件处理工具, 包括提取信息等
 */
@Slf4j
public class NovelUtil {

    public static List<String> extractTitleAndAuthor(String fileName) {
        // example fileName {title} 作者 {author}.txt
        if (fileName == null || !fileName.endsWith(".txt")) {
            return null;
        }
        if (fileName.contains("/")) {
            fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
        }
        if (fileName.contains("\\")) {
            fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
        }
        String namePart = fileName.substring(0, fileName.length() - 4); // remove .txt
        String[] parts = namePart.split(" 作者 ");
        if (parts.length != 2) {
            return null;
        }
        return List.of(parts[0].trim(), parts[1].trim());
    }

    public static long countWordsInFile(File file) {
        try {
            // 1. 检测编码
            String charset = CharsetUtil.detectCharset(file);

            // 2. 非 UTF-8 则转换
            if (!"UTF-8".equalsIgnoreCase(charset)) {
                file = CharsetUtil.convertToUtf8(file, charset);
                log.info("Converted file to UTF-8: {}", file.getAbsolutePath());
            }

            long count = 0;

            try (Reader reader = new InputStreamReader(
                    new FileInputStream(file), StandardCharsets.UTF_8)) {

                char[] buffer = new char[2048];
                int len;
                while ((len = reader.read(buffer)) != -1) {
                    count += len;
                }
            }
            return count;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


}

