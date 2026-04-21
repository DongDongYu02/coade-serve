package cn.dong.coade.modules.cmt.domain.query;

import cn.dong.nexus.common.constants.GlobalConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Schema(description = "请假时长查询对象")
public class AttendLeaveDurationQuery {

    @Schema(description = "用户EKP ID")
    private String userEkpId;

    @Schema(description = "请假开始时间")
    @DateTimeFormat(pattern = GlobalConstants.DatePattern.Y_M_D_H_M)
    private LocalDateTime beginTime;

    @Schema(description = "请假结束时间")
    @DateTimeFormat(pattern = GlobalConstants.DatePattern.Y_M_D_H_M)
    private LocalDateTime endTime;


}
