package cn.dong.nexus.modules.rbac.domain.dto;

import cn.dong.nexus.core.base.BaseDTO;
import cn.dong.nexus.core.valid.BizValidate;
import cn.dong.nexus.modules.rbac.domain.entity.SysRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "角色管理 DTO")
public class SysRoleDTO extends BaseDTO<SysRole> {

    @NotBlank
    @Schema(description = "角色名称")
    @BizValidate.Unique(message = "角色名称已存在！")
    private String name;

    @NotBlank
    @Schema(description = "角色编码")
    @BizValidate.Unique(message = "角色编码已存在！")
    private String code;

    @NotNull
    @Schema(description = "状态 0禁用 1启用")
    private Integer status;

    @Size(max = 64)
    @Schema(description = "角色描述")
    private String description;

}
