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

package com.arctel.mms.service;

import com.arctel.common.baseDTO.QueryPage;
import com.arctel.common.utils.Result;
import com.arctel.domain.dao.entity.MmsNovelFile;
import com.arctel.domain.dto.input.UMmsPageInput;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.IOException;

/**
 * UMmsNovelBizService, 原始文件处理服务
 *
 * @author Arctel
 * @date 2024-06-10
 */
public interface MmsNovelFileService extends IService<MmsNovelFile> {

    /**
     * 获取本地小说文件分页列表
     * @param input
     * @return
     */
    Result<QueryPage<MmsNovelFile>> getLocalNovelFilePageList(UMmsPageInput input) throws IOException;

}
