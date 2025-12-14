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

import com.arctel.common.baseDTO.QueryPage;
import com.arctel.common.utils.FileUtil;
import com.arctel.common.utils.Result;
import com.arctel.domain.dao.entity.MmsNovelFile;
import com.arctel.domain.dao.mapper.MmsNovelFileMapper;
import com.arctel.domain.dto.input.UMmsPageInput;
import com.arctel.mms.service.MmsNovelFileService;
import com.arctel.oms.utils.PublicParamSupport;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * @author Arctel
 * @date 2024-06-10
 */
@Service
public class MmsNovelFileServiceImpl extends ServiceImpl<MmsNovelFileMapper, MmsNovelFile>
        implements MmsNovelFileService {
    @Resource
    PublicParamSupport publicParamSupport;

    @Override
    public Result<QueryPage<MmsNovelFile>> getLocalNovelFilePageList(UMmsPageInput input) throws IOException {
        String paramValueByCode = (String) publicParamSupport.getParamValueByCode(1001);
        List<Path> allTxtFiles = FileUtil.getAllTxtFiles(paramValueByCode);
        System.out.printf("Found %d txt files in directory: %s%n", allTxtFiles.size(), paramValueByCode);
        return null;
    }
}
