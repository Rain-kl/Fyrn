package com.arctel.oms.biz.job.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.arctel.oms.biz.job.domain.OmsJob;
import com.arctel.oms.biz.job.service.OmsJobService;
import com.arctel.oms.biz.job.mapper.OmsJobMapper;
import org.springframework.stereotype.Service;

/**
* @author hspcadmin
* @description 针对表【oms_job(任务表)】的数据库操作Service实现
* @createDate 2025-12-16 11:27:55
*/
@Service
public class OmsJobServiceImpl extends ServiceImpl<OmsJobMapper, OmsJob>
    implements OmsJobService{

}




