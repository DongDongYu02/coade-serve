package cn.dong.coade.modules.cmt.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "处理节点概览 VO")
public class IssueDemandProcessOverviewVO {

    @Schema(description = "待处理数")
    private Long pendingHandleCount = 0L;

    @Schema(description = "待处理超过72小时数量")
    private Long pendingHandleOver72hCount = 0L;

    @Schema(description = "评估中数量")
    private Long evaluatingCount = 0L;

    @Schema(description = "平均响应时长，单位：天")
    private BigDecimal avgResponseDays;

    @Schema(description = "平均响应时长格式化文本")
    private String avgResponseDaysFormat = "-";

    @Schema(description = "开发中数量")
    private Long developingCount = 0L;

    @Schema(description = "开发中计划内数量")
    private Long developingInPlanCount = 0L;

    @Schema(description = "待验收数量")
    private Long pendingAcceptanceCount = 0L;

    @Schema(description = "验收逾期数量")
    private Long acceptanceOverdueCount = 0L;
}