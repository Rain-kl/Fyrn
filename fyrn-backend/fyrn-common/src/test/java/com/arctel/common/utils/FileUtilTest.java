package com.arctel.common.utils;

import com.arctel.oms.utils.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

@Slf4j
class FileUtilTest {

    @Test
    void cosineSimilarity() {

        InputStream is = FileUtil.readFile("/Users/ryan/DEV/tmp/kirara/ta.txt");
        InputStream is2 = FileUtil.readFile("/Users/ryan/DEV/tmp/kirara/tb.txt");

        double v = FileUtil.similarity(is, is2);
        log.info(String.valueOf(v));

    }
}