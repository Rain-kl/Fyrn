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

package com.arctel.fyrn.task.handler;

import com.arctel.common.utils.NovelUtil;
import com.arctel.fyrn.constants.MmsHandlerTagConstant;
import com.arctel.fyrn.entity.MmsNovel;
import com.arctel.fyrn.entity.MmsNovelFile;
import com.arctel.fyrn.mapper.MmsNovelMapper;
import com.arctel.fyrn.service.MmsNovelFileService;
import com.arctel.fyrn.service.MmsNovelService;
import com.arctel.oms.common.base.BaseQueryPage;
import com.arctel.oms.infrastructure.task.DefaultTaskMessage;
import com.arctel.oms.infrastructure.task.OmsTaskHandler;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class OssToMmsHandler extends OmsTaskHandler<DefaultTaskMessage> {

    @Resource
    MmsNovelFileService mmsNovelFileService;

    @Resource
    MmsNovelService mmsNovelService;

    @Resource
    MmsNovelMapper mmsNovelMapper;

    @Override
    public String getHandlerId() {
        return MmsHandlerTagConstant.OssToMms;
    }

    @Override
    public void handleTask(DefaultTaskMessage taskMsg) throws IOException {

        int pageSize = 100;
        int pageNum = 1;
        super.logInfo("开始同步UMMS物料到MMS...");
        while (true) {
            BaseQueryPage<MmsNovelFile> unlinkedMmsNovelFile = mmsNovelFileService.getUnlinkedMmsNovelFile(pageNum, pageSize);
            super.logInfo("处理第 " + pageNum + " 页, 共 " + unlinkedMmsNovelFile.getTotal() + " 条未关联小说的物料记录.");
            if (unlinkedMmsNovelFile.getTotal() <= 0) {
                break;
            }
            this.setTotalProgress(unlinkedMmsNovelFile.getTotal());

            unlinkedMmsNovelFile.getRows().forEach(novelFile -> {
                String fileName = novelFile.getFileName();
                List<String> novelBasicMetadata = NovelUtil.extractTitleAndAuthor(fileName);
                if (novelBasicMetadata == null) {
                    super.updateProgress("提取小说基本信息失败，文件名: " + fileName);
                    mmsNovelFileService.markUnableAutoBind(novelFile.getId());
                    return;
                }
                // 查询小说是否存在
                String title = novelBasicMetadata.get(0);
                String author = novelBasicMetadata.get(1);
                MmsNovel mmsNovel = mmsNovelMapper.selectOne(new LambdaQueryWrapper<MmsNovel>().eq(MmsNovel::getNovelTitle, title).eq(MmsNovel::getNovelAuthor, author));
                // 如果小说不存在，则新增小说记录
                if (mmsNovel == null) {
                    MmsNovel newmmsNovel = new MmsNovel();
                    newmmsNovel.setNovelTitle(title);
                    newmmsNovel.setNovelAuthor(author);
                    try {
                        mmsNovelService.createNovel(newmmsNovel, novelFile);
                        super.updateProgress("新增物料信息: " + fileName + ", 小说ID: " + novelFile.getNovelId());
                    } catch (Exception e) {
                        super.logError("创建物料失败，文件名: " + fileName + ", 错误信息: " + e.getMessage(), e);
                    }
                } else {
                    novelFile.setNovelId(mmsNovel.getId());
                    mmsNovelFileService.update(novelFile, new LambdaQueryWrapper<MmsNovelFile>().eq(MmsNovelFile::getId, novelFile.getId()));
                    super.updateProgress("更新物料信息: " + fileName + ", 小说ID: " + novelFile.getNovelId());
                }
            });
        }

    }

}