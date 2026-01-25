package com.arctel.mms.service.impl;

import com.arctel.mms.service.MmsMetaService;
import com.arctel.oms.pub.base.BaseQueryPage;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.arctel.domain.dao.entity.MmsMeta;
import com.arctel.domain.dao.mapper.MmsMetaMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author ryan
* @description 针对表【mms_meta(小说元数据表)】的数据库操作Service实现
* @createDate 2026-01-25 21:02:27
*/
@Service
public class MmsMetaServiceImpl extends ServiceImpl<MmsMetaMapper, MmsMeta>
    implements MmsMetaService {

    @Override
    public BaseQueryPage<MmsMeta> pageMmsMeta(MmsMeta mmsMeta, Integer pageNo, Integer pageSize) {
//        设置分页
        IPage<MmsMeta> page = new Page<>(pageNo, pageSize);

        IPage<MmsMeta> result = page(
                page,
                new LambdaQueryWrapper<MmsMeta>()
                        .eq(mmsMeta.getId() != null,
                                MmsMeta::getId, mmsMeta.getId())
                        .like(mmsMeta.getTitle() != null,
                                MmsMeta::getTitle, mmsMeta.getTitle())
                        .like(mmsMeta.getAuthor() != null,
                                MmsMeta::getAuthor, mmsMeta.getAuthor())
                        .orderByDesc(MmsMeta::getId)
        );

        List<MmsMeta> ordersList = result.getRecords();


        return new BaseQueryPage<>(result.getTotal(), pageSize, pageNo, ordersList);
    }
}




