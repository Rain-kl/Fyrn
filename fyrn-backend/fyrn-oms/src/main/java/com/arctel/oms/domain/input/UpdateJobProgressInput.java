package com.arctel.oms.domain.input;

import com.arctel.oms.common.base.BaseInput;
import com.arctel.oms.domain.dto.JobProgressDto;
import lombok.Data;


@Data
public class UpdateJobProgressInput extends BaseInput {
    String jobId;
    JobProgressDto jobProgressDto;
}
