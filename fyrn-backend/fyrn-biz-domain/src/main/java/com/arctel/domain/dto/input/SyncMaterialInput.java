package com.arctel.domain.dto.input;

import com.arctel.oms.common.base.BaseInput;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SyncMaterialInput extends BaseInput {

    public Integer size = 0;

}
