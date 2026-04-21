package cn.dong.coade.modules.cmt.domain.query;

import cn.dong.nexus.common.constants.GlobalConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "外出工时查询对象")
@Data
public class AttendOutgoingDurationQuery {

    @Schema(description = "用户EKP ID")
    @NotBlank
    private String userEkpId;

    @Schema(description = "外出日期")
    @DateTimeFormat(pattern = GlobalConstants.DatePattern.NORMAL_ONLY_DATE)
    private LocalDate outDate;

    @Schema(description = "外出开始时间")
    @DateTimeFormat(pattern = GlobalConstants.DatePattern.TIME)
    private LocalTime outTimeBegin;

    @Schema(description = "结束结束时间")
    @DateTimeFormat(pattern = GlobalConstants.DatePattern.TIME)
    private LocalTime outTimeEnd;
}
