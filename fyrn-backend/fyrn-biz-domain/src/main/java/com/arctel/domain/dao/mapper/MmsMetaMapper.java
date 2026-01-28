package com.arctel.domain.dao.mapper;
import java.util.List;

import com.arctel.domain.dao.entity.MmsMeta;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author ryan
* @description 针对表【mms_meta(小说元数据表)】的数据库操作Mapper
* @createDate 2026-01-25 21:02:27
* @Entity com.arctel.domain.dao.entity.MmsMeta
*/
public interface MmsMetaMapper extends BaseMapper<MmsMeta> {

    List<MmsMeta> findAll();

}




