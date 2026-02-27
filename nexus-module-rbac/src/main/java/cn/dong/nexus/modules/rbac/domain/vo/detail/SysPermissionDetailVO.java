package cn.dong.nexus.modules.rbac.domain.vo.detail;

import cn.dong.nexus.core.base.BaseDetailVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "权限管理 详情 VO")
public class SysPermissionDetailVO extends BaseDetailVO {

    @Schema(description = "上级 ID")
    private String pid;

    @Schema(description = "权限名称")
    private String name;

    @Schema(pattern = "路由地址")
    private String path;

    @Schema(pattern = "重定向地址")
    private String redirect;

    @Schema(pattern = "页面组件")
    private String component;

    @Schema(pattern = "权限标识")
    private String authCode;

    @Schema(pattern = "权限类型 1目录 2菜单 3按钮")
    private Integer type;

    @Schema(pattern = "图标")
    private String icon;

    @Schema(pattern = "状态 0禁用 1启用")
    private Integer status;

    @Schema(pattern = "是否固定标签栏 0否 1是")
    private Integer affix;

    @Schema(pattern = "是否缓存页面 0否 1是")
    private Integer keepAlive;

    @Schema(pattern = "是否隐藏 0否 1是")
    private Integer hidden;

    @Schema(pattern = "排序")
    private Integer sort;
}
