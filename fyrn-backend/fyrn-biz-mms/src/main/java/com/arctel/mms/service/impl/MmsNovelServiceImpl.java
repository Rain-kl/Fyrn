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

import com.arctel.common.utils.NovelUtil;
import com.arctel.domain.dao.entity.MmsNovel;
import com.arctel.domain.dao.entity.MmsNovelFile;
import com.arctel.domain.dao.mapper.MmsNovelMapper;
import com.arctel.mms.service.MmsNovelFileService;
import com.arctel.mms.service.MmsNovelService;
import com.arctel.oms.biz.file.OosService;
import com.arctel.oms.pub.base.BaseQueryPage;
import com.arctel.oms.support.PublicParamSupport;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class MmsNovelServiceImpl extends ServiceImpl<MmsNovelMapper, MmsNovel>
        implements MmsNovelService {

    @Resource
    OosService oosSupport;

    @Resource
    PublicParamSupport publicParamSupport;

    @Resource
    MmsNovelFileService mmsNovelFileService;

    @Resource
    MmsNovelMapper mmsNovelMapper;

    @Resource
    MmsNovelServiceImpl self;


    @Override
    public BaseQueryPage<MmsNovel> pageMmsNovel(MmsNovel mmsNovel, Integer pageNo, Integer pageSize) {
//        设置分页
        IPage<MmsNovel> page = new Page<>(pageNo, pageSize);

        IPage<MmsNovel> result = page(
                page,
                new LambdaQueryWrapper<MmsNovel>()
                        .like(mmsNovel.getNovelTitle() != null,
                                MmsNovel::getNovelTitle, mmsNovel.getNovelTitle())
                        .like(mmsNovel.getNovelAuthor() != null,
                                MmsNovel::getNovelAuthor, mmsNovel.getNovelAuthor())
                        .orderByDesc(MmsNovel::getId)
        );
//
        List<MmsNovel> ordersList = result.getRecords();


        return new BaseQueryPage<>(result.getTotal(), pageSize, pageNo, ordersList);
    }


    @Override
    public void syncJob() {
        int pageSize = 100;
        int pageNum = 1;
        while (true) {
            BaseQueryPage<MmsNovelFile> unlinkedMmsNovelFile = mmsNovelFileService.getUnlinkedMmsNovelFile(pageNum, pageSize);
            if (unlinkedMmsNovelFile.getTotal() <= 0) {
                break;
            }
            List<MmsNovelFile> novelFiles = unlinkedMmsNovelFile.getRows();
            unlinkedMmsNovelFile.getRows().forEach(novelFile -> {
                String fileName = novelFile.getFileName();
                List<String> novelBasicMetadata = NovelUtil.extractTitleAndAuthor(fileName);

                // 查询小说是否存在
                String title = novelBasicMetadata.get(0);
                String author = novelBasicMetadata.get(1);
                MmsNovel mmsNovel = mmsNovelMapper.selectOne(
                        new LambdaQueryWrapper<MmsNovel>()
                                .eq(MmsNovel::getNovelTitle, title)
                                .eq(MmsNovel::getNovelAuthor, author)
                );
                // 如果小说不存在，则新增小说记录
                if (mmsNovel == null) {
                    MmsNovel newmmsNovel = new MmsNovel();
                    newmmsNovel.setNovelTitle(title);
                    newmmsNovel.setNovelAuthor(author);
                    novelFile.setNovelId(newmmsNovel.getId());
                    try {
                        self.createNovel(newmmsNovel, novelFile);
                    } catch (Exception e) {
                        log.error("创建小说失败, 跳过!", e);
                    }
                } else {
                    novelFile.setNovelId(mmsNovel.getId());
                    mmsNovelFileService.update(novelFile,
                            new LambdaQueryWrapper<MmsNovelFile>()
                                    .eq(MmsNovelFile::getId, novelFile.getId())
                    );
                }
            });
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void createNovel(MmsNovel newmmsNovel, MmsNovelFile mmsNovelFile) {
        mmsNovelMapper.insert(newmmsNovel);
        mmsNovelFileService.update(mmsNovelFile,
                new LambdaQueryWrapper<MmsNovelFile>()
                        .eq(MmsNovelFile::getId, mmsNovelFile.getId())
        );
    }


}
