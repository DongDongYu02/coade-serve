package cn.dong.coade.modules.cmt.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "问题/优化反馈系统分布统计")
public class IssueDemandSystemDistributionVO {

    @Schema(description = "系统类型")
    private Integer systemType;

    @Schema(description = "系统类型文本")
    private String systemTypeText;

    @Schema(description = "数量")
    private Long count;
}
