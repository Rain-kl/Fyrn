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

package com.arctel.mms.service.impl;

import com.arctel.domain.dto.input.SyncMaterialInput;
import com.arctel.oms.common.base.BaseQueryPage;
import com.arctel.oms.service.OosService;
import com.arctel.oms.common.utils.FileUtil;
import com.arctel.common.utils.NovelUtil;
import com.arctel.oms.common.utils.PagingUtil;
import com.arctel.oms.common.utils.Result;
import com.arctel.domain.dao.entity.MmsNovel;
import com.arctel.domain.dao.entity.MmsNovelFile;
import com.arctel.domain.dao.mapper.MmsNovelFileMapper;
import com.arctel.domain.dao.mapper.MmsNovelMapper;
import com.arctel.domain.dto.LocalFileSimpleDTO;
import com.arctel.domain.dto.input.UMmsPageInput;
import com.arctel.mms.service.MmsNovelFileService;
import com.arctel.oms.support.PublicParamSupport;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * @author Arctel
 * @date 2024-06-10
 */
@Service
public class MmsNovelFileServiceImpl extends ServiceImpl<MmsNovelFileMapper, MmsNovelFile>
        implements MmsNovelFileService {
    @Resource
    PublicParamSupport publicParamSupport;

    @Resource
    MmsNovelMapper mmsNovelMapper;

    @Resource
    OosService oosSupport;

    @Override
    public Result<BaseQueryPage<LocalFileSimpleDTO>> getUnprocessedLocalFile(UMmsPageInput input) throws IOException {
        String paramValueByCode = (String) publicParamSupport.getParamValueByCode(1001);
        List<Path> allTxtFiles = FileUtil.getAllTxtFiles(paramValueByCode);

        BaseQueryPage<Path> page = PagingUtil.page(
                allTxtFiles, input.getPageNo(), input.getPageSize(),
                Comparator.comparing(Path::getFileName), Objects::nonNull
        );
        BaseQueryPage<LocalFileSimpleDTO> localFileSimpleDTOQueryPage = new BaseQueryPage<>();
        localFileSimpleDTOQueryPage.setCurrentPage(page.getCurrentPage());
        localFileSimpleDTOQueryPage.setPageSize(page.getPageSize());
        localFileSimpleDTOQueryPage.setTotal(page.getTotal());

        List<LocalFileSimpleDTO> localFileSimpleDTOS = page.getRows().stream().map(file -> {
            LocalFileSimpleDTO localFileSimpleDTO = new LocalFileSimpleDTO();
            localFileSimpleDTO.setFileName(file.getFileName().toString());
            localFileSimpleDTO.setFilePath(file.toString());
            localFileSimpleDTO.setFileSize(FileUtil.getFileSize(file));
            localFileSimpleDTO.setWordCount(NovelUtil.countWordsInFile(new File(file.toString())));
            return localFileSimpleDTO;
        }).toList();

        localFileSimpleDTOQueryPage.setRows(localFileSimpleDTOS);

        return Result.success(localFileSimpleDTOQueryPage);
    }

    @Override
    public Result<String> syncMaterial(SyncMaterialInput input) throws IOException {
        Integer size = input.getSize();
        if(size == null || size <= 0) {
            size = Integer.MAX_VALUE;
        }
        UMmsPageInput uMmsPageInput = new UMmsPageInput();
        uMmsPageInput.setPageSize(size);
        uMmsPageInput.setPageNo(1);
        getUnprocessedLocalFile(uMmsPageInput);
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void deleteProcessedFile() {

    }

    public void syncJob() throws IOException {
        String paramValueByCode = (String) publicParamSupport.getParamValueByCode(1001);
        List<Path> allTxtFiles = FileUtil.getAllTxtFiles(paramValueByCode);
        allTxtFiles.forEach(file -> {
            Path fileName = file.getFileName();
            System.out.println(fileName);
            List<String> novelBasicMetadata = NovelUtil.extractTitleAndAuthor(fileName.toString());

            // 查询小说是否存在
            MmsNovel mmsNovel = mmsNovelMapper.selectOne(
                    new LambdaQueryWrapper<MmsNovel>()
                            .eq(MmsNovel::getNovelTitle, novelBasicMetadata.get(0))
                            .eq(MmsNovel::getNovelAuthor, novelBasicMetadata.get(1))
            );
            // 如果小说不存在，则新增小说记录, 并且直接上传 OOS
            if (mmsNovel == null) {
                MmsNovelFile mmsNovelFile = new MmsNovelFile();
                mmsNovelFile.setFileName(fileName.toString());
                mmsNovelFile.setFileSize(FileUtil.getFileSize(file));
                mmsNovelFile.setWordCount(NovelUtil.countWordsInFile(new File(file.toString())));

                String oosPath = oosSupport.upload(
                        FileUtil.fileToByteArray(file.toFile()),
                        fileName.toString()
                );
                mmsNovelFile.setFilePath(oosPath);

            } else {
                //如果小说存在, 则下载文件进行对比, 如果相似度高于 0.95, 则认为是重复文件, 否则新增为其他插入 OOS 库中, 更新小说下载地址记录
            }

        });
    }


}
