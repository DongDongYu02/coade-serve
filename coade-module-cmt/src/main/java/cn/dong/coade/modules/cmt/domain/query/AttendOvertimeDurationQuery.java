package cn.dong.coade.modules.cmt.domain.query;

import cn.dong.nexus.common.constants.GlobalConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "加班工时查询对象")
@Data
public class AttendOvertimeDurationQuery {

    @Schema(description = "用户EKP ID")
    @NotBlank
    private String userEkpId;

    @Schema(description = "加班日期")
    private LocalDate overtimeDate;

    @Schema(description = "加班开始时间")
    @DateTimeFormat(pattern = GlobalConstants.DatePattern.TIME)
    private LocalTime beginTime;

    @Schema(description = "加班结束时间")
    @DateTimeFormat(pattern = GlobalConstants.DatePattern.TIME)
    private LocalTime endTime;
}
