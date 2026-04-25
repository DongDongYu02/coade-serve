package cn.dong.coade.modules.cmt.domain.dto;

import cn.dong.nexus.common.constants.GlobalConstants;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Schema(description = "加班申请EKP回调 DTO")
public class AttendOvertimeRequestEkpCallbackDTO {

    @Schema(description = "加班日期")
    @NotNull
    private LocalDate overtimeDate;

    @Schema(description = "加班开始时间")
    @NotNull
    @JsonFormat(pattern = GlobalConstants.DatePattern.TIME, timezone = GlobalConstants.ZoneTime.GMT8)
    private LocalTime beginTime;

    @Schema(description = "加班结束时间")
    @NotNull
    @JsonFormat(pattern = GlobalConstants.DatePattern.TIME, timezone = GlobalConstants.ZoneTime.GMT8)
    private LocalTime endTime;

    @Schema(description = "加班事由")
    @NotBlank
    private String reason;

    @Schema(description = "加班时长")
    private BigDecimal duration;

    @Schema(description = "用户EKPID")
    private String userEkpId;

    @Schema(description = "EKP流程ID")
    private String ekpReviewId;

    @Schema(description = "审批状态")
    @NotNull
    private Integer status;

}
