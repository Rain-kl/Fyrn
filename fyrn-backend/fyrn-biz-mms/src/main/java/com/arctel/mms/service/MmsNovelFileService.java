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

import com.arctel.domain.dto.input.SyncMaterialInput;

import com.arctel.domain.dao.entity.MmsNovelFile;
import com.arctel.domain.dto.LocalFileSimpleDTO;
import com.arctel.domain.dto.input.UMmsPageInput;
import com.arctel.oms.pub.base.BaseQueryPage;
import com.arctel.oms.pub.utils.Result;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

/**
 * UMmsNovelBizService, 原始文件处理服务
 *
 * @author Arctel
 * @date 2024-06-10
 */
public interface MmsNovelFileService extends IService<MmsNovelFile> {

    BaseQueryPage<MmsNovelFile> pageMmsNovelFile(MmsNovelFile mmsNovelFile, Integer pageNo, Integer pageSize);

    BaseQueryPage<MmsNovelFile> getUnlinkedMmsNovelFile(Integer pageNo, Integer pageSize);

    Result<BaseQueryPage<LocalFileSimpleDTO>> getUnprocessedLocalFile(UMmsPageInput input) throws IOException;

    Result<String> syncMaterial(SyncMaterialInput input) throws IOException;

    void syncLocalFile(LocalFileSimpleDTO localFileSimpleDTO, String operator) throws IOException;

    void downloadNovelFile(String filePath);

    ResponseEntity<byte[]> downloadMaterial(String mmsNovelFileId);
}
