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

package net.arctel.fyrn.service.impl;

import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.lang.UUID;
import net.arctel.common.constants.ParameterConstant;
import net.arctel.common.utils.NovelUtil;
import net.arctel.fyrn.dto.LocalFileSimpleDTO;
import net.arctel.fyrn.entity.MmsNovel;
import net.arctel.fyrn.entity.MmsNovelFile;
import net.arctel.fyrn.input.BindNovelFileInput;
import net.arctel.fyrn.input.UMmsPageInput;
import net.arctel.fyrn.mapper.MmsNovelFileMapper;
import net.arctel.fyrn.mapper.MmsNovelMapper;
import net.arctel.fyrn.output.DownloadResult;
import net.arctel.fyrn.service.MmsNovelFileService;
import net.arctel.fyrn.service.MmsNovelService;
import net.arctel.oms.common.base.BaseQueryPage;
import net.arctel.oms.common.constants.ErrorConstant;
import net.arctel.platform.framework.exception.BizException;
import net.arctel.oms.common.utils.FileUtil;
import net.arctel.oms.common.utils.PagingUtil;
import net.arctel.oms.service.OmsParameterService;
import net.arctel.oms.service.OmsStorageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Arctel
 * @since 2024-06-10
 */
@Service
@Slf4j
public class MmsNovelFileServiceImpl extends ServiceImpl<MmsNovelFileMapper, MmsNovelFile> implements MmsNovelFileService {

    public static final String OOS_FILE_PATH = "/novels";

    @Resource
    private MmsNovelFileService self;

    @Resource
    OmsParameterService publicParamSupport;

    @Resource
    OmsStorageService oosSupport;

    @Resource
    private MmsNovelService mmsNovelService;

    @Resource
    private MmsNovelMapper mmsNovelMapper;

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

        IPage<MmsNovelFile> result = page(page, new LambdaQueryWrapper<MmsNovelFile>().eq(mmsNovelFile.getNovelId() != null, MmsNovelFile::getNovelId, mmsNovelFile.getNovelId()).like(mmsNovelFile.getFileName() != null, MmsNovelFile::getFileName, mmsNovelFile.getFileName()).orderByDesc(MmsNovelFile::getId));

        List<MmsNovelFile> ordersList = result.getRecords();
        return new BaseQueryPage<>(result.getTotal(), pageSize, pageNo, ordersList);
    }

    @Override
    public BaseQueryPage<MmsNovelFile> getUnlinkedMmsNovelFile(Integer pageNo, Integer pageSize) {
        IPage<MmsNovelFile> page = new Page<>(pageNo, pageSize);

        // 查询 novelId 为空的文件, novelId在数据库中为null表示未绑定到mms
        IPage<MmsNovelFile> result = page(page, new LambdaQueryWrapper<MmsNovelFile>().isNull(MmsNovelFile::getNovelId).orderByDesc(MmsNovelFile::getId));

        List<MmsNovelFile> ordersList = result.getRecords();
        return new BaseQueryPage<>(result.getTotal(), pageSize, pageNo, ordersList);
    }


    @Override
    public BaseQueryPage<LocalFileSimpleDTO> getUnprocessedLocalFile(UMmsPageInput input) throws IOException {
        String paramValueByCode = (String) publicParamSupport.getParamValueByCode(ParameterConstant.UMMS_FILE_PATH);
        List<Path> allTxtFiles = FileUtil.getAllTxtFiles(paramValueByCode);

        BaseQueryPage<Path> page = PagingUtil.page(allTxtFiles, input.getPageNo(), input.getPageSize(), Comparator.comparing(Path::getFileName), Objects::nonNull);
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

        return localFileSimpleDTOQueryPage;
    }


    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void syncLocalFile(LocalFileSimpleDTO localFileSimpleDTO, String operator) throws IOException {
        String fileName = localFileSimpleDTO.getFileName();
        String filePath = localFileSimpleDTO.getFilePath();
        // 1. 创建数据库记录
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

        // 2. 同步文件到对象存储
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
        String rubbishPath = (String) publicParamSupport.getParamValueByCode(ParameterConstant.MMS_RUBBISH_PATH);
        if (StringUtils.isBlank(rubbishPath)) {
            FileUtil.deleteFile(file);
        } else {
            try {
                Files.move(file.toPath(), Path.of(rubbishPath, file.getName()));
            } catch (Exception e) {
                // 移动失败则重命名 +.bk
                File renamedFile = new File(file.getAbsolutePath() + ".bk");
                if (!file.renameTo(renamedFile)) {
                    throw new IOException("Failed to rename file: " + file.getAbsolutePath());
                }
            }
        }
    }
    /**
     * 下载素材 - 返回文件数据和响应头信息，不直接返回 ResponseEntity
     */
    @Override
    public DownloadResult downloadMaterial(String mmsNovelFileId) {
        // 1. 获取数据库记录
        MmsNovelFile mmsNovelFile = getById(mmsNovelFileId);
        if (mmsNovelFile == null) {
            throw new BizException(ErrorConstant.DOWNLOAD_FAILED, "Downloaded file is empty");
        }

        // 2. 从对象存储下载字节数组
        byte[] fileBytes;
        try {
            fileBytes = oosSupport.downloadBytes(mmsNovelFile.getFilePath());
        } catch (Exception e) {
            throw new BizException(ErrorConstant.DOWNLOAD_FAILED, e, "Failed to download file from OOS");
        }

        if (fileBytes == null || fileBytes.length == 0) {
            throw new BizException(ErrorConstant.DOWNLOAD_FAILED, "Downloaded file is empty");
        }

        // 3. 构建文件名和响应头信息
        String fileName = mmsNovelFile.getFileName();
        Long novelId = mmsNovelFile.getNovelId();
        if (novelId != null) {
            MmsNovel mmsNovel = mmsNovelMapper.selectById(novelId);
            fileName = NovelUtil.buildNovelFileName(mmsNovel.getNovelTitle(), mmsNovel.getNovelAuthor());
        }

        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20"); // 防止空格变加号

        // 4. 构建响应头配置（不包含 ResponseEntity 相关类）
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE);
        headers.put("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
        headers.put("Content-Length", String.valueOf(fileBytes.length));

        return DownloadResult.success(fileBytes, headers, encodedFileName);
    }

    @Override
    public Boolean bindNovelFile(BindNovelFileInput input) {
        Long novelId = input.getNovelId();
        MmsNovelFile file = getOne(new LambdaQueryWrapper<MmsNovelFile>().eq(MmsNovelFile::getId, input.getFileId()));
        if (file == null) {
            throw new BizException(ErrorConstant.COMMON_ERROR, "未找到对应的物料文件记录，无法绑定小说ID: " + novelId);
        }

        if (novelId != null && novelId > 0) {
            // 绑定到已有小说
            boolean exists = mmsNovelMapper.exists(new LambdaQueryWrapper<MmsNovel>().eq(MmsNovel::getId, novelId));
            if (!exists) {
                throw new BizException(ErrorConstant.COMMON_ERROR, "绑定的小说ID不存在: " + novelId);
            }
            file.setNovelId(novelId);
            updateById(file);
        } else {
            // 创建新小说并绑定
            MmsNovel novel = new MmsNovel();
            novel.setNovelTitle(input.getNovelTitle());
            novel.setNovelAuthor(input.getNovelAuthor());
            // 假设此方法会设置 novel.id 并更新 file.novelId
            mmsNovelService.createNovel(novel, file);
        }
        return true;
    }

    @Override
    public Boolean markUnableAutoBind(Long mmsNovelFileId) {
        return lambdaUpdate().set(MmsNovelFile::getNovelId, -1L).eq(MmsNovelFile::getId, mmsNovelFileId).update();
    }

    @Override
    public Boolean deleteFile(String fileId) {
        if (!exists(new LambdaQueryWrapper<MmsNovelFile>().eq(MmsNovelFile::getId, fileId))) {
            throw new BizException(ErrorConstant.COMMON_ERROR, "未找到对应的物料文件记录，无法删除，fileId: " + fileId);
        }
        // 删除数据库记录
        return removeById(fileId);
    }
}
