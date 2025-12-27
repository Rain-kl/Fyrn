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

import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.lang.UUID;
import com.arctel.oms.biz.file.OosService;
import com.arctel.oms.biz.job.JobRunnable;
import com.arctel.oms.biz.job.ThreadPoolJobService;
import com.arctel.common.utils.NovelUtil;
import com.arctel.domain.dto.input.SyncMaterialInput;

import com.arctel.domain.dao.entity.MmsNovelFile;
import com.arctel.domain.dao.mapper.MmsNovelFileMapper;
import com.arctel.domain.dto.LocalFileSimpleDTO;
import com.arctel.domain.dto.input.UMmsPageInput;
import com.arctel.mms.service.MmsNovelFileService;

import com.arctel.oms.pub.base.BaseQueryPage;
import com.arctel.oms.pub.domain.OmsJob;
import com.arctel.oms.pub.domain.dto.JobProgressDto;
import com.arctel.oms.pub.domain.input.CreateJobInput;
import com.arctel.oms.pub.domain.input.UpdateJobProgressInput;
import com.arctel.oms.pub.utils.FileUtil;
import com.arctel.oms.pub.utils.PagingUtil;
import com.arctel.oms.pub.utils.Result;
import com.arctel.oms.support.PublicParamSupport;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Arctel
 * @since 2024-06-10
 */
@Service
public class MmsNovelFileServiceImpl extends ServiceImpl<MmsNovelFileMapper, MmsNovelFile>
        implements MmsNovelFileService {

    public static final String OOS_FILE_PATH = "/novels";

    @Resource
    private MmsNovelFileService self;

    @Resource
    PublicParamSupport publicParamSupport;

    @Resource
    OosService oosSupport;

    @Resource
    ThreadPoolJobService threadPoolJobService;

    /**
     * 查询物料分页列表
     *
     * @param mmsNovelFile 接受filename, novelId查询条件
     * @param pageNo
     * @param pageSize
     * @return
     */
    @Override
    public BaseQueryPage<MmsNovelFile> pageMmsNovelFile(MmsNovelFile mmsNovelFile, Integer pageNo, Integer pageSize) {
        IPage<MmsNovelFile> page = new Page<>(pageNo, pageSize);

        IPage<MmsNovelFile> result = page(
                page,
                new LambdaQueryWrapper<MmsNovelFile>()
                        .like(mmsNovelFile.getFileName() != null,
                                MmsNovelFile::getFileName, mmsNovelFile.getFileName())
                        .like(mmsNovelFile.getNovelId() != null,
                                MmsNovelFile::getNovelId, mmsNovelFile.getNovelId())
                        .orderByDesc(MmsNovelFile::getId)
        );

        List<MmsNovelFile> ordersList = result.getRecords();
        return new BaseQueryPage<>(result.getTotal(), pageSize, pageNo, ordersList);
    }

    @Override
    public BaseQueryPage<MmsNovelFile> getUnlinkedMmsNovelFile(Integer pageNo, Integer pageSize) {
        IPage<MmsNovelFile> page = new Page<>(pageNo, pageSize);

        IPage<MmsNovelFile> result = page(
                page,
                new LambdaQueryWrapper<MmsNovelFile>()
                        .isNull(MmsNovelFile::getNovelId)
                        .orderByDesc(MmsNovelFile::getId)
        );

        List<MmsNovelFile> ordersList = result.getRecords();
        return new BaseQueryPage<>(result.getTotal(), pageSize, pageNo, ordersList);
    }


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
        if (size == null || size <= 0) {
            size = Integer.MAX_VALUE;
        }
        UMmsPageInput uMmsPageInput = new UMmsPageInput();
        uMmsPageInput.setPageSize(size);
        uMmsPageInput.setPageNo(1);
        Result<BaseQueryPage<LocalFileSimpleDTO>> unprocessedLocalFile = getUnprocessedLocalFile(uMmsPageInput);
        List<LocalFileSimpleDTO> rows = unprocessedLocalFile.getData().getRows();

        OmsJob job = threadPoolJobService.createJob(new CreateJobInput("sync_material", "同步物料到OOS"),
                new JobRunnable(threadPoolJobService) {
                    @Override
                    public void taskRun() {
                        rows.forEach(f -> {
                            try {
                                // 通过 self 调用，确保走 Spring 代理，事务生效
                                self.syncLocalFile(f, input.getOperator());
                            } catch (Exception e) {
                                // 记录失败日志或更新 job 状态，但不中断其他文件处理
                                updateProgress(rows.size(),"Failed to process file: " + f.getFileName() + ", error: " + e.getMessage());
                                return;
                            }
                            updateProgress(rows.size(),"Processed: " + f.getFileName());
                        });
                    }
                });
        return Result.success(job.getJobId());
    }


    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void syncLocalFile(LocalFileSimpleDTO localFileSimpleDTO, String operator) throws IOException {
        String fileName = localFileSimpleDTO.getFileName();
        String filePath = localFileSimpleDTO.getFilePath();
        File file = new File(filePath);
        String suffix = FileNameUtil.getSuffix(fileName);
        UUID uuid = UUID.randomUUID();
        String oosPath = OOS_FILE_PATH + "/" + uuid + (suffix.isEmpty() ? "" : "." + suffix);
        MmsNovelFile mmsNovelFile = new MmsNovelFile();
        mmsNovelFile.setFileName(fileName);
        mmsNovelFile.setFilePath(oosPath);
        mmsNovelFile.setFileSize(FileUtil.getFileSize(file.toPath()));
        mmsNovelFile.setWordCount(NovelUtil.countWordsInFile(file));
        mmsNovelFile.setCreatedUser(operator);
        mmsNovelFile.setUpdatedUser(operator);
        baseMapper.insert(mmsNovelFile);

        byte[] fileBytes = FileUtil.fileToByteArray(file);
        oosSupport.upload(fileBytes, oosPath);
        deleteProcessedFile(file);
    }

    /**
     * 删除已处理的本地文件
     *
     * @param file 文件
     */
    public void deleteProcessedFile(File file) throws IOException {
        String rubbishPath = (String) publicParamSupport.getParamValueByCode(1002);
        if (StringUtils.isBlank(rubbishPath)) {
            FileUtil.deleteFile(file);
        } else {
            try {
                Files.move(
                        file.toPath(),
                        Path.of(rubbishPath, file.getName())
                );
            } catch (Exception e) {
                // 移动失败则重命名 +.bk
                File renamedFile = new File(file.getAbsolutePath() + ".bk");
                if (!file.renameTo(renamedFile)) {
                    throw new IOException("Failed to rename file: " + file.getAbsolutePath());
                }
            }
        }
    }

    @Override
    public void downloadNovelFile(String filePath) {
        byte[] fileBytes = oosSupport.downloadBytes(filePath);
        System.out.printf("Downloaded file size: %d bytes%n", fileBytes.length);
        String preview = new String(fileBytes, 0, Math.min(fileBytes.length, 100));
        System.out.println("File preview (first 100 characters):");
        System.out.println(preview);
    }

}
