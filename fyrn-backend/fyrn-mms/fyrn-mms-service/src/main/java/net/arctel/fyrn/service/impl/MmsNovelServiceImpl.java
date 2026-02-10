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


import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.arctel.fyrn.entity.MmsNovel;
import net.arctel.fyrn.entity.MmsNovelFile;
import net.arctel.fyrn.mapper.MmsNovelFileMapper;
import net.arctel.fyrn.mapper.MmsNovelMapper;
import net.arctel.fyrn.service.MmsNovelFileService;
import net.arctel.fyrn.service.MmsNovelService;
import net.arctel.oms.common.base.BaseQueryPage;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import jakarta.annotation.Resource;


@Service
public class MmsNovelServiceImpl extends ServiceImpl<MmsNovelMapper, MmsNovel>
        implements MmsNovelService {


    @Resource
    MmsNovelFileService mmsNovelFileService;

    @Resource
    MmsNovelMapper mmsNovelMapper;

    @Resource
    private MmsNovelFileMapper mmsNovelFileMapper;


    @Override
    public BaseQueryPage<MmsNovel> pageMmsNovel(MmsNovel mmsNovel, Integer pageNo, Integer pageSize) {
//        设置分页
        IPage<MmsNovel> page = new Page<>(pageNo, pageSize);

        IPage<MmsNovel> result = page(
                page,
                new LambdaQueryWrapper<MmsNovel>()
                        .eq(mmsNovel.getId() != null,
                                MmsNovel::getId, mmsNovel.getId())
                        .like(mmsNovel.getNovelTitle() != null,
                                MmsNovel::getNovelTitle, mmsNovel.getNovelTitle())
                        .like(mmsNovel.getNovelAuthor() != null,
                                MmsNovel::getNovelAuthor, mmsNovel.getNovelAuthor())
                        .orderByDesc(MmsNovel::getId)
        );

        List<MmsNovel> ordersList = result.getRecords();


        return new BaseQueryPage<>(result.getTotal(), pageSize, pageNo, ordersList);
    }

    @Override
    public List<MmsNovelFile> getMmsNovelFile(String mmsNovelId) {
        return mmsNovelFileMapper.selectList(
                new LambdaQueryWrapper<MmsNovelFile>()
                        .eq(MmsNovelFile::getNovelId, mmsNovelId
                        ));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void createNovel(MmsNovel newmmsNovel, MmsNovelFile mmsNovelFile) {
        mmsNovelMapper.insert(newmmsNovel);
        mmsNovelFile.setNovelId(newmmsNovel.getId());
        mmsNovelFileService.update(mmsNovelFile,
                new LambdaQueryWrapper<MmsNovelFile>()
                        .eq(MmsNovelFile::getId, mmsNovelFile.getId())
        );
    }


}
