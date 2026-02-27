package cn.dong.nexus.modules.rbac.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "角色选择 VO")
public class RoleSelectionVO {
    @Schema(description = "角色 ID")
    private String id;

    @Schema(description = "角色名称")
    private String name;

    @Schema(description = "状态")
    private Integer status;
}
