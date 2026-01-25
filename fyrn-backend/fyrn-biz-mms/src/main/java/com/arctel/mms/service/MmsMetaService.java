package com.arctel.mms.service;

import com.arctel.domain.dao.entity.MmsMeta;
import com.arctel.oms.pub.base.BaseQueryPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author ryan
* @description 针对表【mms_meta(小说元数据表)】的数据库操作Service
* @createDate 2026-01-25 21:02:27
*/

public interface MmsMetaService extends IService<MmsMeta> {

    BaseQueryPage<MmsMeta> pageMmsMeta(MmsMeta mmsMeta, Integer pageNo, Integer pageSize);
}
