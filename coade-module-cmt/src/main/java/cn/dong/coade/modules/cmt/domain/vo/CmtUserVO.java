package cn.dong.coade.modules.cmt.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "CMT用户 VO")
public class CmtUserVO {

    @Schema(description = "ID")
    private String id;

    @Schema(description = "蓝凌 ID")
    private String ekpId;

    @Schema(description = "名称")
    private String username;

    @Schema(description = "部门 ID")
    private String deptId;

    @Schema(description = "部门名称")
    private String dept;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "企微 ID")
    private String weComId;

}
