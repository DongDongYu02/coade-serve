package cn.dong.coade.modules.cmt.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Schema(description = "外出申请EKP回调 DTO")
public class AttendOutgoingRequestEkpCallbackDTO {
    @Schema(description = "外出日期")
    private LocalDate outDate;

    @Schema(description = "外出开始时间")
    private LocalTime outTimeBegin;

    @Schema(description = "外出结束时间")
    private LocalTime outTimeEnd;

    @Schema(description = "外出事由")
    private String reason;

    @Schema(description = "外出时长")
    private BigDecimal duration;

    @Schema(description = "用户EKPID")
    private String userEkpId;

    @Schema(description = "EKP流程ID")
    @NotNull
    private String ekpReviewId;

    @Schema(description = "审批状态")
    @NotNull
    private Integer status;
}
