package cn.dong.coade.modules.cmt.domain.vo;

import cn.dong.nexus.common.constants.GlobalConstants;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Schema(description = "加班申请记录 VO")
public class AttendOvertimeRequestVO {

    @Schema(description = "ID")
    private String id;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "加班日期")
    private LocalDate overtimeDate;

    @Schema(description = "加班开始时间 yyyy-MM-dd HH:mm")
    @JsonFormat(pattern = GlobalConstants.DatePattern.TIME, timezone = GlobalConstants.ZoneTime.GMT8)
    private LocalTime beginTime;

    @Schema(description = "加班结束时间 yyyy-MM-dd HH:mm")
    @JsonFormat(pattern = GlobalConstants.DatePattern.TIME, timezone = GlobalConstants.ZoneTime.GMT8)
    private LocalTime endTime;

    @Schema(description = "加班时长")
    private BigDecimal duration;

    @Schema(description = "状态 0审批中 1审批通过 2审批驳回 3已撤销")
    private Integer status;

    @Schema(description = "加班事由")
    private String reason;
}
