package cn.dong.coade.modules.cmt.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "职员提出数统计TOP VO")
public class IssueDemandProposerTopVO {

    @Schema(description = "提出人ID")
    private String proposeUserId;

    @Schema(description = "提出人名称")
    private String proposeUserName;

    @Schema(description = "提出部门")
    private String proposeDept;

    @Schema(description = "提出数量")
    private Long total;

    @Schema(description = "排名")
    private Integer ranking;
}
