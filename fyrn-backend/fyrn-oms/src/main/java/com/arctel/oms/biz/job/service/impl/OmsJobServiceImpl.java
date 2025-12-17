package com.arctel.oms.biz.job.service.impl;

import com.arctel.oms.common.base.BaseQueryPage;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.arctel.oms.biz.job.domain.OmsJob;
import com.arctel.oms.biz.job.service.OmsJobService;
import com.arctel.oms.biz.job.mapper.OmsJobMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author hspcadmin
* @description 针对表【oms_job(任务表)】的数据库操作Service实现
* @createDate 2025-12-16 11:27:55
*/
@Service
public class OmsJobServiceImpl extends ServiceImpl<OmsJobMapper, OmsJob>
    implements OmsJobService{

    @Override
    public BaseQueryPage<OmsJob> pageJob(OmsJob omsJob, Integer pageNo, Integer pageSize) {
        //        设置分页
        IPage<OmsJob> page = new Page<>(pageNo, pageSize);

        IPage<OmsJob> result = page(
                page,
                new LambdaQueryWrapper<OmsJob>()
                        .like(omsJob.getJobId() != null,
                                OmsJob::getJobId, omsJob.getJobId())
                        .like(omsJob.getTaskType() != null,
                                OmsJob::getTaskType, omsJob.getTaskType())
                        .like(omsJob.getStatus() != null,
                                OmsJob::getStatus, omsJob.getStatus())
                        .orderByDesc(OmsJob::getId)
        );
//
        List<OmsJob> ordersList = result.getRecords();


        return new BaseQueryPage<>(result.getTotal(), pageSize, pageNo, ordersList);
    }
}




