package com.arctel.oms.domain.input;

import com.arctel.oms.common.base.BaseInput;
import lombok.Data;

@Data
public class UpdateJobInput extends BaseInput {
    String jobId;
    Integer status;
    String message;
}
