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

package com.arctel.oms.pub.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class FileUtil {

    public static List<Path> getAllTxtFiles(String dirPath) throws IOException {
        try (Stream<Path> paths = Files.walk(Paths.get(dirPath))) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".txt"))
                    .collect(Collectors.toList());
        }
    }

    public static long getFileSize(Path path) {
        try {
            return Files.size(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static InputStream stringToStream(String str) {
        return new ByteArrayInputStream(
                str.getBytes(StandardCharsets.UTF_8));

    }

    /**
     * 删除文件或目录
     *
     * @param file
     */
    public static void deleteFile(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    deleteFile(f);
                }
            }
        }
        if (!file.delete()) {
            log.warn("Failed to delete file: {}", file.getAbsolutePath());
        }
    }


    /**
     * 计算两个输入流的综合相似度
     * 结合余弦相似度(权重6)和文件大小相似度(权重4)
     *
     * @param is1 第一个输入流
     * @param is2 第二个输入流
     * @return 综合相似度 [0, 1]
     */
    public static double similarity(InputStream is1, InputStream is2) {
        try {
            // 读取两个流的内容到字节数组
            byte[] bytes1 = readAllBytes(is1);
            byte[] bytes2 = readAllBytes(is2);

            // 计算文件大小相似度
            double sizeSim = calculateSizeSimilarity(bytes1.length, bytes2.length);

            // 将字节数组转换回流用于余弦相似度计算
            InputStream stream1 = new ByteArrayInputStream(bytes1);
            InputStream stream2 = new ByteArrayInputStream(bytes2);

            // 计算余弦相似度
            double cosineSim = cosineSimilarity(stream1, stream2);

            // 加权综合: 余弦权重6, 大小权重4
            double weightedSimilarity = (cosineSim * 6 + sizeSim * 4) / 10.0;

            return weightedSimilarity;
        } catch (IOException e) {
            log.error("Error calculating similarity", e);
            return 0.0;
        }
    }

    /**
     * 计算基于文件大小的相似度
     * 使用公式: Sim(s1,s2) = exp(-d/τ)
     * 其中 d = |ln(s1) - ln(s2)|
     * τ = 1 (当大小相差一倍时,相似度为0.5)
     *
     * @param size1 第一个文件大小(bytes)
     * @param size2 第二个文件大小(bytes)
     * @return 大小相似度 [0, 1]
     */
    private static double calculateSizeSimilarity(long size1, long size2) {
        if (size1 <= 0 || size2 <= 0) {
            return 0.0;
        }

        // 计算 d = |ln(s1) - ln(s2)| = |ln(s1/s2)|
        double d = Math.abs(Math.log(size1) - Math.log(size2));

        // τ = 0.05 (使得大小相差约20倍时,相似度约为0.1)
        double tau = 0.05;

        // Sim(s1,s2) = exp(-d/τ)
        return Math.exp(-d / tau);
    }

    /**
     * 读取输入流的所有字节
     *
     * @param is 输入流
     * @return 字节数组
     * @throws IOException IO异常
     */
    private static byte[] readAllBytes(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int bytesRead;
        while ((bytesRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytesRead);
        }
        return buffer.toByteArray();
    }

    /**
     * 计算两个文本文件的余弦相似度
     *
     * @param is1
     * @param is2
     * @return
     */
    private static double cosineSimilarity(InputStream is1, InputStream is2) {
        Map<String, Integer> v1 = wordFreq(is1);
        Map<String, Integer> v2 = wordFreq(is2);

        if (v1.isEmpty() || v2.isEmpty()) {
            return 0.0;
        }

        Set<String> words = new HashSet<>();
        words.addAll(v1.keySet());
        words.addAll(v2.keySet());

        int dot = 0;
        double norm1 = 0, norm2 = 0;

        for (String w : words) {
            int a = v1.getOrDefault(w, 0);
            int b = v2.getOrDefault(w, 0);
            dot += a * b;
            norm1 += a * a;
            norm2 += b * b;
        }

        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }

        return dot / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }


    private static Map<String, Integer> wordFreq(InputStream is) {
        Map<String, Integer> freq = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String cleaned = line.replaceAll("[^\\p{IsHan}A-Za-z0-9]+", "");
                int n = 2; // 2-gram
                if (cleaned.length() < n)
                    continue;

                for (int i = 0; i <= cleaned.length() - n; i++) {
                    String token = cleaned.substring(i, i + n);
                    freq.merge(token, 1, Integer::sum);
                }
            }
        } catch (IOException e) {
            log.error("Error reading input stream", e);
            throw new RuntimeException(e);
        }
        return freq;
    }

    public static InputStream readFile(String filePath) {
        try {
            return new FileInputStream(filePath);

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 文件转字节数组
     *
     * @param file 文件
     * @return 字节数组
     */
    public static byte[] fileToByteArray(File file) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             FileInputStream fis = new FileInputStream(file)) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
