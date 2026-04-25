package cn.dong.coade.modules.cmt.domain.vo;

import cn.dong.nexus.common.constants.GlobalConstants;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "出差申请记录 VO")
public class AttendBizTripRequestVO {

    @Schema(description = "ID")
    private String id;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "出差开始时间 yyyy-MM-dd HH:mm")
    @JsonFormat(pattern = GlobalConstants.DatePattern.Y_M_D_H_M, timezone = GlobalConstants.ZoneTime.GMT8)
    private LocalDateTime beginTime;

    @Schema(description = "出差结束时间 yyyy-MM-dd HH:mm")
    @JsonFormat(pattern = GlobalConstants.DatePattern.Y_M_D_H_M, timezone = GlobalConstants.ZoneTime.GMT8)
    private LocalDateTime endTime;

    @Schema(description = "出差时长")
    private BigDecimal duration;

    @Schema(description = "出差时长显示值")
    private String durationFormat;

    @Schema(description = "状态 0审批中 1审批通过 2审批驳回 3已撤销")
    private Integer status;

    @Schema(description = "出差事由")
    private String reason;
}
