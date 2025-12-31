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

package com.arctel.mms.controller;

import com.arctel.domain.dto.input.BindNovelFileInput;
import com.arctel.domain.dto.input.SyncMaterialInput;
import com.arctel.domain.dto.input.UMmsNovelPageInput;
import com.arctel.domain.dao.entity.MmsNovelFile;
import com.arctel.domain.dto.LocalFileSimpleDTO;
import com.arctel.domain.dto.input.UMmsPageInput;
import com.arctel.mms.service.MmsNovelFileService;
import com.arctel.oms.pub.base.BaseQueryPage;
import com.arctel.oms.pub.domain.OmsJob;
import com.arctel.oms.pub.utils.Result;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

import java.io.IOException;

@RequestMapping("/umms")
@RestController
@Validated
public class UMmsNovelController {

    @Resource
    MmsNovelFileService uMmsNovelService;


    /**
     * 获取未处理的小说列表
     *
     * @return
     */
    @GetMapping("/local/page")
    public Result<BaseQueryPage<LocalFileSimpleDTO>> localPage(UMmsPageInput input) throws IOException {
        return uMmsNovelService.getUnprocessedLocalFile(input);
    }

    /**
     * 查询物料分页列表
     *
     * @param input
     * @return
     */
    @GetMapping("/novel/page")
    public Result<BaseQueryPage<MmsNovelFile>> novelPage(UMmsNovelPageInput input) {

        MmsNovelFile mmsNovelFile = new MmsNovelFile();
        BeanUtils.copyProperties(input, mmsNovelFile);

        BaseQueryPage<MmsNovelFile> mmsNovelQueryPage = uMmsNovelService.pageMmsNovelFile(
                mmsNovelFile, input.getPageNo(), input.getPageSize());
        return Result.success(mmsNovelQueryPage);
    }

    /**
     * 物料文件绑定
     * 不存在则新增，存在则更新
     * @param input
     * @return
     * @throws IOException
     */
    @PostMapping("/novel/bind")
    public Result<Boolean> bindNovelFile(BindNovelFileInput input) throws IOException {
        return Result.success(uMmsNovelService.bindNovelFile(input));
    }

    /**
     * 删除物料文件
     * @param fileId
     * @return
     */
    @PostMapping("/novel/delete")
    public Result<Boolean> deleteFile(String fileId) {
        return Result.success(uMmsNovelService.deleteFile(fileId));
    }



    /**
     * 同步素材到 oos
     *
     * @param input
     * @return
     * @throws IOException
     */
    @GetMapping("/syncMaterial")
    public Result<String> syncMaterial(SyncMaterialInput input) throws IOException {
        return uMmsNovelService.syncMaterial(input);
    }

    /**
     * 下载物料
     */
    @GetMapping("/downloadMaterial")
    public ResponseEntity<byte[]> downloadMaterial(String mmsNovelFileId) {
        return uMmsNovelService.downloadMaterial(mmsNovelFileId);
    }


    /**
     * 文件去重
     */
    @PostMapping("/dedup")
    public Result<OmsJob> dedup() {
        //TODO: 实现文件去重功能
        return null;
    }



}
