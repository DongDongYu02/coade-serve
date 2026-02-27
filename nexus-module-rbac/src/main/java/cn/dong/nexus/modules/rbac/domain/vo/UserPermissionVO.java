package cn.dong.nexus.modules.rbac.domain.vo;

import cn.hutool.core.lang.tree.Tree;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class UserPermissionVO {

    @Schema(description = "用户权限菜单树")
    private List<Tree<String>> permissions;

    @Schema(description = "用户权限按钮集合")
    private List<String> authCodes;

    @Schema(description = "用户首页路由地址")
    private String index;

    public UserPermissionVO empty() {
        this.permissions = List.of();
        this.authCodes = List.of();
        this.index = "/";
        return this;
    }
}
