package cn.dong.nexus.modules.rbac.domain.dto;

import cn.dong.nexus.core.base.BaseDTO;
import cn.dong.nexus.core.valid.BizValidate;
import cn.dong.nexus.core.valid.ValidGroup;
import cn.dong.nexus.modules.rbac.domain.entity.SysUser;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "用户 DTO")
public class SysUserDTO extends BaseDTO<SysUser> {

    @NotBlank
    @BizValidate.Unique(message = "用户名已存在！")
    @Schema(description = "用户名")
    private String username;

    @NotBlank(groups = ValidGroup.Create.class)
    @Schema(description = "密码")
    private String password;

    @NotBlank
    @BizValidate.Unique(message = "昵称已存在！")
    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "手机号")
    @BizValidate.Unique(message = "手机号已存在！")
    private String phone;

    @Schema(description = "状态 0禁用 1启用")
    @NotNull
    private Integer status;

    @Schema(description = "头像")
    private String avatar;

    @NotEmpty
    @Schema(description = "角色ID 集合")
    private List<String> roleIds;

}
