package com.arctel.oms.pub.domain.dto;

import lombok.Data;

@Data
public class JobOverviewDto {

    Integer totalJobCount;
    Integer runningJobCount;
    Integer successJobCount;
    Integer failedJobCount;

}
