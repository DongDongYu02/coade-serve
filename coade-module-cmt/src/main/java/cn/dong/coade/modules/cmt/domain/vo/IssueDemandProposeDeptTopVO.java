package cn.dong.coade.modules.cmt.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "部门提出数排行 VO")
@Data
public class IssueDemandProposeDeptTopVO {

    @Schema(description = "部门")
    private String proposeDept;

    @Schema(description = "总数")
    private Long total;

    @Schema(description = "有效数")
    private Long validTotal;

    @Schema(description = "无效数")
    private Long invalidTotal;

    @Schema(description = "有效率")
    private BigDecimal effectiveRate;

    @Schema(description = "有效率格式化文本")
    private String effectiveRateText;

    @Schema(description = "排名")
    private Integer ranking;


}
