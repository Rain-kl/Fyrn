package com.arctel.common.utils;


import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;

@Slf4j
class NovelUtilTest {

    @Test
    void extractTitleAndAuthor() {
        List<String> strings = NovelUtil.extractTitleAndAuthor("user/data/年后了 作者 为空.txt");
        log.info(strings.toString());
        assert strings.size() == 2;
        assert strings.get(0).equals("年后了");
        assert strings.get(1).equals("为空");
    }
}