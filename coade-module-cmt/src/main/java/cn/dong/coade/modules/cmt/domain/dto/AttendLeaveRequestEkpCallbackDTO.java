package cn.dong.coade.modules.cmt.domain.dto;

import cn.dong.nexus.common.constants.GlobalConstants;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "请假申请EKP回调 DTO")
public class AttendLeaveRequestEkpCallbackDTO {
    @Schema(description = "请假类型 2事假 3病假")
    @NotNull
    private Integer type;

    @Schema(description = "请假开始时间")
    @NotNull
    @JsonFormat(pattern = GlobalConstants.DatePattern.Y_M_D_H_M, timezone = GlobalConstants.ZoneTime.GMT8)
    private LocalDateTime beginTime;

    @Schema(description = "请假结束时间")
    @NotNull
    @JsonFormat(pattern = GlobalConstants.DatePattern.Y_M_D_H_M, timezone = GlobalConstants.ZoneTime.GMT8)
    private LocalDateTime endTime;

    @Schema(description = "请假原因")
    @NotBlank
    private String reason;

    @Schema(description = "请假时长")
    private BigDecimal duration;

    @Schema(description = "用户EKPID")
    private String userEkpId;

    @Schema(description = "EKP流程ID")
    private String ekpReviewId;

    @Schema(description = "审批状态")
    @NotNull
    private Integer status;

}
