package com.arctel.oms.domain.input;

import com.arctel.oms.common.base.BaseInput;
import com.arctel.oms.domain.dto.JobProgressDto;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class UpdateJobProgressInput extends BaseInput {
    String jobId;
    String log;
    JobProgressDto jobProgressDto;
}
