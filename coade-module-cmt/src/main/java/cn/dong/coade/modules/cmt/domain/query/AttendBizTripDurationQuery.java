package cn.dong.coade.modules.cmt.domain.query;

import cn.dong.nexus.common.constants.GlobalConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Schema(description = "出差工时查询对象")
@Data
public class AttendBizTripDurationQuery {

    @Schema(description = "用户EKP ID")
    @NotBlank
    private String userEkpId;

    @Schema(description = "出差开始时间")
    @DateTimeFormat(pattern = GlobalConstants.DatePattern.NORMAL_ONLY_DATE)
    private LocalDate beginTime;

    @Schema(description = "出差结束时间")
    @DateTimeFormat(pattern = GlobalConstants.DatePattern.NORMAL_ONLY_DATE)
    private LocalDate endTime;
}
