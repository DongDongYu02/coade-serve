package cn.dong.coade.modules.cmt.domain.dto;

import cn.dong.nexus.common.constants.GlobalConstants;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "出差申请EKP回调 DTO")
public class AttendBizTripRequestEkpCallbackDTO {

    @Schema(description = "出差开始时间")
    @NotNull
    @JsonFormat(pattern = GlobalConstants.DatePattern.NORMAL_ONLY_DATE, timezone = GlobalConstants.ZoneTime.GMT8)
    private LocalDate beginTime;

    @Schema(description = "出差结束时间")
    @NotNull
    @JsonFormat(pattern = GlobalConstants.DatePattern.NORMAL_ONLY_DATE, timezone = GlobalConstants.ZoneTime.GMT8)
    private LocalDate endTime;

    @Schema(description = "外出事由")
    @NotBlank
    private String reason;

    @Schema(description = "出差时长")
    private BigDecimal duration;

    @Schema(description = "出差时长显示值")
    private String durationFormat;

    @Schema(description = "用户EKPID")
    private String userEkpId;

    @Schema(description = "EKP流程ID")
    private String ekpReviewId;

    @Schema(description = "审批状态")
    @NotNull
    private Integer status;

}
