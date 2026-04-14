package cn.dong.coade.modules.cmt.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "负责人选择 VO")
public class IssueDemandPrincipalUserSelectionVO {

    @Schema(description = "负责人ID")
    private String id;

    @Schema(description = "负责人名称")
    private String name;
}
