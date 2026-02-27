package cn.dong.nexus.modules.rbac.domain.dto;

import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.core.base.BaseDTO;
import cn.dong.nexus.core.valid.BizValidate;
import cn.dong.nexus.modules.rbac.domain.entity.SysPermission;
import cn.hutool.core.util.StrUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "权限管理 DTO")
public class SysPermissionDTO extends BaseDTO<SysPermission> {

    @NotBlank
    @Schema(description = "权限名称")
    @BizValidate.Unique(message = "权限名称已存在！")
    private String name;

    @Schema(description = "上级 ID")
    private String pid;

    @Schema(description = "路由地址")
    private String path;

    @Schema(description = "重定向地址")
    private String redirect;

    @Schema(description = "组件路径")
    private String component;

    @Schema(description = "权限编码")
    private String authCode;

    @NotNull
    @Schema(description = "权限类型")
    private Integer type;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "状态 0禁用 1启用")
    private Integer status;

    @Schema(description = "是否固定标签栏 0否 1是")
    private Integer affix;

    @Schema(description = "是否缓存页面 0否 1是")
    private Integer keepAlive;

    @Schema(description = "是否隐藏 0否 1是")
    private Integer hidden;

    @Schema(description = "排序")
    private Integer sort;


    @Override
    public void doValidate() {
        super.doValidate();
        if (StrUtil.isBlank(pid)) {
            this.pid = GlobalConstants.ROOT_ID;
        }
    }
}
