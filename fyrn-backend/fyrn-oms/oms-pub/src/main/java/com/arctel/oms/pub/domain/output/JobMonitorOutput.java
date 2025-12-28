package com.arctel.oms.pub.domain.output;

import com.arctel.oms.pub.domain.dto.JobOverviewDto;
import com.arctel.oms.pub.domain.dto.ThreadPoolMetricsDTO;
import lombok.Data;

@Data
public class JobMonitorOutput {

    ThreadPoolMetricsDTO threadPoolMetricsDTO;

    JobOverviewDto jobOverviewDto;

}
