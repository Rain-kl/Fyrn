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
