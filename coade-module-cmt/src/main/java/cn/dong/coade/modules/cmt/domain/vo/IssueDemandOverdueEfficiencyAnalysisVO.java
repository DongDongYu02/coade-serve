package cn.dong.coade.modules.cmt.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class IssueDemandOverdueEfficiencyAnalysisVO {

    @Schema(description = "开发逾期数")
    private Long devOverdueTotal;

    @Schema(description = "验收逾期数")
    private Long acceptanceOverdueTotal;

    @Schema(description = "平均响应时长展示文本")
    private String avgResponseDurationText;


    @Schema(description = "平均完成耗时展示文本")
    private String avgFinishCostDurationText;

}
