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

package com.arctel.domain.dto.input;

import com.arctel.domain.dao.entity.MmsNovel;
import com.arctel.domain.dao.entity.MmsNovelFile;
import lombok.Data;

@Data
public class BindNovelFileInput {

    /**
     * 文件ID
     * 被绑定的id mmsFile Id
     */
    public Long fileId;

    /**
     * 小说ID
     * 存在则为绑定操作，不存在则为新增绑定操作
     */
    public Long novelId;

    /**
     * 小说名称
     */
    private String novelTitle;

    /**
     * 小说作者
     */
    private String novelAuthor;

}
