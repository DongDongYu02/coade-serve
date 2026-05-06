package cn.dong.coade.modules.cmt.domain.vo;

import cn.dong.coade.modules.cmt.domain.bo.EkpApprovalCurrentNodeBO;
import cn.dong.nexus.common.constants.GlobalConstants;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "补卡申请详情 VO")
public class AttendReissueDetailVO {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "用户ID")
    private String cmtUserId;

    @Schema(description = "实际打卡时间 yyyy-MM-dd HH:mm")
    @JsonFormat(pattern = GlobalConstants.DatePattern.Y_M_D_H_M, timezone = GlobalConstants.ZoneTime.GMT8)
    private LocalDateTime checkinTime;

    @Schema(description = "规则打卡时间 yyyy-MM-dd HH:mm")
    @JsonFormat(pattern = GlobalConstants.DatePattern.Y_M_D_H_M, timezone = GlobalConstants.ZoneTime.GMT8)
    private LocalDateTime ruleCheckinTime;

    @Schema(description = "异常原因")
    private String reason;

    @Schema(description = "是否为特殊原因补卡  0否1是")
    private Integer isSpecialCase;

    @Schema(description = "是否审批通过 0否 1是")
    private Integer isApproved;

    @Schema(description = "本月剩余补卡次数")
    private String usedReissueFrequency;

    @Schema(description = "申请时间")
    @JsonFormat(pattern = GlobalConstants.DatePattern.Y_M_D_H_M, timezone = GlobalConstants.ZoneTime.GMT8)
    private LocalDateTime createTime;

    @Schema(description = "当前审批节点")
    private EkpApprovalCurrentNodeBO currentNode;

    @Schema(description = "异常类型")
    private String reissueType;


}
