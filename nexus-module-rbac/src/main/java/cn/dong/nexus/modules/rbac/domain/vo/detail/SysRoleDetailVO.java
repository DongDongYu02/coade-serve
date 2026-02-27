package cn.dong.nexus.modules.rbac.domain.vo.detail;

import cn.dong.nexus.core.base.BaseDetailVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "角色详情 VO")
public class SysRoleDetailVO extends BaseDetailVO {

    @Schema(description = "权限名称")
    private String name;

    @Schema(description = "角色编码")
    private String code;

    @Schema(description = "角色描述")
    private String description;

    @Schema(description = "状态 0启用 1禁用")
    private Integer status;
}
