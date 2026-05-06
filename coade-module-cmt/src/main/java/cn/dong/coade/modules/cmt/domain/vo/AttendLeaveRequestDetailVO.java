package cn.dong.coade.modules.cmt.domain.vo;

import cn.dong.coade.modules.cmt.domain.bo.EkpApprovalCurrentNodeBO;
import cn.dong.nexus.common.constants.GlobalConstants;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "请假申请详情 VO")
public class AttendLeaveRequestDetailVO {

    @Schema(description = "ID")
    private String id;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "请假类型 2事假 3病假")
    private String type;

    @Schema(description = "请假开始时间 yyyy-MM-dd HH:mm")
    @JsonFormat(pattern = GlobalConstants.DatePattern.Y_M_D_H_M, timezone = GlobalConstants.ZoneTime.GMT8)
    private LocalDateTime beginTime;

    @Schema(description = "请假结束时间 yyyy-MM-dd HH:mm")
    @JsonFormat(pattern = GlobalConstants.DatePattern.Y_M_D_H_M, timezone = GlobalConstants.ZoneTime.GMT8)
    private LocalDateTime endTime;

    @Schema(description = "请假时长")
    private BigDecimal duration;

    @Schema(description = "请假时长显示值")
    private String durationFormat;

    @Schema(description = "状态 0审批中 1审批通过 2审批驳回 3已撤销")
    private Integer status;

    @Schema(description = "请假原因")
    private String reason;

    @Schema(description = "申请时间")
    @JsonFormat(pattern = GlobalConstants.DatePattern.Y_M_D_H_M, timezone = GlobalConstants.ZoneTime.GMT8)
    private LocalDateTime createTime;

    @Schema(description = "当前审批节点")
    private EkpApprovalCurrentNodeBO currentNode;

}
