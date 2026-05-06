package cn.dong.coade.modules.cmt.domain.vo;

import cn.dong.coade.modules.cmt.domain.bo.EkpApprovalCurrentNodeBO;
import cn.dong.nexus.common.constants.GlobalConstants;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Schema(description = "外出申请记录详情 VO")
public class AttendOutgoingRequestDetailVO {

    @Schema(description = "ID")
    private String id;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "外出日期 yyyy-MM-dd")
    @JsonFormat(pattern = GlobalConstants.DatePattern.NORMAL_ONLY_DATE, timezone = GlobalConstants.ZoneTime.GMT8)
    private LocalDate outDate;

    @Schema(description = "外出开始时间 HH:mm")
    @JsonFormat(pattern = GlobalConstants.DatePattern.TIME, timezone = GlobalConstants.ZoneTime.GMT8)
    private LocalTime outTimeBegin;

    @Schema(description = "结束结束时间 HH:mm")
    @JsonFormat(pattern = GlobalConstants.DatePattern.TIME, timezone = GlobalConstants.ZoneTime.GMT8)
    private LocalTime outTimeEnd;

    @Schema(description = "请假时长")
    private BigDecimal duration;

    @Schema(description = "状态 0审批中 1审批通过 2审批驳回 3已撤销")
    private Integer status;

    @Schema(description = "外出事由")
    private String reason;

    @Schema(description = "申请时间")
    @JsonFormat(pattern = GlobalConstants.DatePattern.Y_M_D_H_M, timezone = GlobalConstants.ZoneTime.GMT8)
    private LocalDateTime createTime;

    @Schema(description = "当前节点")
    private EkpApprovalCurrentNodeBO currentNode;

}
