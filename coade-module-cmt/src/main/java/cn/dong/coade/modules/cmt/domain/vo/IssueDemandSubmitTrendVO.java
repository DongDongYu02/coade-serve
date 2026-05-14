package cn.dong.coade.modules.cmt.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "问题/需求上报趋势 VO")
public class IssueDemandSubmitTrendVO {

    @Schema(description = "日期")
    private String date;

    @Schema(description = "问题/优化反馈数")
    private Long issueCount;

    @Schema(description = "需求数")
    private Long demandCount;
}
