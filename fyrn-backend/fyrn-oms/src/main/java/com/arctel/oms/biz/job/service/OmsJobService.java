package com.arctel.oms.biz.job.service;

import com.arctel.oms.biz.job.domain.OmsJob;
import com.arctel.oms.common.base.BaseQueryPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author hspcadmin
* @description 针对表【oms_job(任务表)】的数据库操作Service
* @createDate 2025-12-16 11:27:56
*/
public interface OmsJobService extends IService<OmsJob> {

    BaseQueryPage<OmsJob> pageJob(OmsJob omsJob, Integer pageNo, Integer pageSize);

}
