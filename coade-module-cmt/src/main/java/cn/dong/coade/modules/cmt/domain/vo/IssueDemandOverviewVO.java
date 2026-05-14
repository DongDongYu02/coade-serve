package cn.dong.coade.modules.cmt.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "需求上报统计概览 VO")
public class IssueDemandOverviewVO {

    @Schema(description = "上报总数")
    private Long totalCount = 0L;

    @Schema(description = "优化反馈数")
    private Long issueFeedbackCount = 0L;

    @Schema(description = "需求开发数")
    private Long demandDevelopmentCount = 0L;

    @Schema(description = "紧急事项数")
    private Long urgentCount = 0L;

    @Schema(description = "待处理数")
    private Long pendingHandleCount = 0L;

    @Schema(description = "评估中数量")
    private Long assessingCount = 0L;

    @Schema(description = "开发中数量")
    private Long devCount = 0L;

    @Schema(description = "处理中数")
    private Long processingCount = 0L;

    @Schema(description = "待验收数")
    private Long pendingAcceptanceCount = 0L;

    @Schema(description = "已完成数")
    private Long completedCount = 0L;

    @Schema(description = "作废数量")
    private Long voidedCount = 0L;

    @Schema(description = "完成率")
    private BigDecimal completionRate = BigDecimal.ZERO;

    @Schema(description = "完成率格式化文本")
    private String completionRateText = "0%";

    @Schema(description = "有效率")
    private BigDecimal effectiveRate = BigDecimal.ZERO;

    @Schema(description = "有效率格式化文本")
    private String effectiveRateText = "0%";


}