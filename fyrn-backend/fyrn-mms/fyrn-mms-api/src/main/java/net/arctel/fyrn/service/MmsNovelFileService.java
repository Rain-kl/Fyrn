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

package net.arctel.fyrn.service;


import net.arctel.fyrn.dto.LocalFileSimpleDTO;
import net.arctel.fyrn.entity.MmsNovelFile;
import net.arctel.fyrn.input.BindNovelFileInput;
import net.arctel.fyrn.input.UMmsPageInput;
import net.arctel.fyrn.output.DownloadResult;
import net.arctel.platform.oms.common.base.BaseQueryPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.IOException;

/**
 * UMmsNovelBizService, 原始文件处理服务
 *
 * @author Arctel
 * @date 2024-06-10
 */
public interface MmsNovelFileService extends IService<MmsNovelFile> {

    BaseQueryPage<MmsNovelFile> pageMmsNovelFile(MmsNovelFile mmsNovelFile, Integer pageNo, Integer pageSize);

    /**
     * 获取未关联小说的原始文件, 即mms_novel_file表中novel_id为空的记录
     */
    BaseQueryPage<MmsNovelFile> getUnlinkedMmsNovelFile(Integer pageNo, Integer pageSize);

    /**
     * 获取未同步到OSS的本地文件
     */
    BaseQueryPage<LocalFileSimpleDTO> getUnprocessedLocalFile(UMmsPageInput input) throws IOException;

    /**
     * 同步本地文件到OSS
     */
    void syncLocalFile(LocalFileSimpleDTO localFileSimpleDTO, String operator) throws IOException;

    /**
     * 下载物料
     *
     * @param mmsNovelFileId 物料ID
     * @return
     */
    DownloadResult downloadMaterial(String mmsNovelFileId);

    /**
     * 绑定小说文件到 MMS
     *
     * @param input 绑定输入对象
     */
    Boolean bindNovelFile(BindNovelFileInput input);

    /**
     * 取消自动绑定小说文件
     *
     * @param mmsNovelFileId 物料 ID
     * @return
     */
    Boolean markUnableAutoBind(Long mmsNovelFileId);

    Boolean deleteFile(String fileId);
}
