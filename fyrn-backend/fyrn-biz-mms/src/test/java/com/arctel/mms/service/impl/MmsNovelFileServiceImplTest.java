package com.arctel.mms.service.impl;

import com.arctel.mms.service.MmsNovelFileService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class MmsNovelFileServiceImplTest {

    @Resource
    MmsNovelFileService mmsNovelFileService;

    @Test
    void downloadNovelFile() {
        mmsNovelFileService.downloadNovelFile("/novels/22cfaafb-fce7-4dc7-ba82-60585d3997b4");
    }
}