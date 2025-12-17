package com.arctel.oms.domain.output;

import com.arctel.oms.common.base.BaseInput;
import com.arctel.oms.domain.OmsJob;
import com.arctel.oms.domain.dto.JobProgressDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GetJobDetailOutput extends OmsJob {

    JobProgressDto jobProgressDto;
}
