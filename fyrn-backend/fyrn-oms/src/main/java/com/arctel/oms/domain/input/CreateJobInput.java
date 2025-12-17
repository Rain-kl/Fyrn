package com.arctel.oms.domain.input;

import com.arctel.oms.common.base.BaseInput;
import lombok.Data;

@Data
public class CreateJobInput extends BaseInput {

    String task_type;

    String message;
}
