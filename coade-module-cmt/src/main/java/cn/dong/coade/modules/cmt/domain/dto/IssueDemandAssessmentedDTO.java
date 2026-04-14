package cn.dong.coade.modules.cmt.domain.dto;

import cn.dong.nexus.common.constants.GlobalConstants;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "完成评估 DTO")
public class IssueDemandAssessmentedDTO {

    @Schema(description = "主键")
    @NotBlank
    private String id;

    @JsonFormat(pattern = GlobalConstants.DatePattern.NORMAL_ONLY_DATE, timezone = GlobalConstants.ZoneTime.GMT8)
    @NotNull
    private LocalDate planFinishTime;
}
