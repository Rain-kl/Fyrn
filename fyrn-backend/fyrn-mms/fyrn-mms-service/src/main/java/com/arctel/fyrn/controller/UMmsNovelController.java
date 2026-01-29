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

package com.arctel.fyrn.controller;


import com.arctel.fyrn.dto.LocalFileSimpleDTO;
import com.arctel.fyrn.entity.MmsNovelFile;
import com.arctel.fyrn.requestion.BindNovelFileInput;
import com.arctel.fyrn.requestion.UMmsNovelPageInput;
import com.arctel.fyrn.requestion.UMmsPageInput;
import com.arctel.fyrn.service.MmsNovelFileService;
import com.arctel.oms.common.base.BaseQueryPage;
import com.arctel.oms.common.utils.Result;
import com.arctel.oms.entity.OmsJob;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
