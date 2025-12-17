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

import com.arctel.oms.common.base.BaseQueryPage;
import com.arctel.oms.common.utils.Result;
import com.arctel.domain.dao.entity.MmsNovelFile;
import com.arctel.domain.dto.LocalFileSimpleDTO;
import com.arctel.domain.dto.input.UMmsPageInput;
import com.arctel.mms.service.MmsNovelFileService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
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

    @GetMapping("/noevl/page")
    public Result<BaseQueryPage<MmsNovelFile>> novelPage(UMmsPageInput input) throws IOException {
        return null;
    }

}
