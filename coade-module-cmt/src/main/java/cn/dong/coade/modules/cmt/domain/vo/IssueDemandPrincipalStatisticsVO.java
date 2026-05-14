package cn.dong.coade.modules.cmt.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "负责人任务统计 VO")
public class IssueDemandPrincipalStatisticsVO {

    @Schema(description ="负责人ID")
    private String principalUserId;

    @Schema(description = "负责人姓名")
    private String principalUserName;

    @Schema(description = "任务数")
    private Long total;

    @Schema(description = "未完成数量")
    private Long unfinishedTotal;

    @Schema(description = "逾期数量")
    private Long overdueTotal;

    @Schema(description = "完成率，例如 0.6840")
    private BigDecimal completionRate;

    @Schema(description = "完成率展示文本，例如 68.4%")
    private String completionRateText;
}
