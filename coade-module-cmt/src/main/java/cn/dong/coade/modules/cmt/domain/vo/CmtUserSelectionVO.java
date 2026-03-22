package cn.dong.coade.modules.cmt.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "CMT用户选择 VO")
public class CmtUserSelectionVO {

    @Schema(description = "ID")
    private String id;

    @Schema(description = "EKP ID")
    private String ekpId;

    @Schema(description = "用户名")
    private String name;

    @Schema(description = "部门")
    private String dept;

}
