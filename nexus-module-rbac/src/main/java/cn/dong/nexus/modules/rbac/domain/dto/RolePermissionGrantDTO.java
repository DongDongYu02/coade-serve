package cn.dong.nexus.modules.rbac.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "角色权限分配 DTO")
public class RolePermissionGrantDTO {

    private String roleId;

    @Schema(description = "权限 ID列表")
    @NotEmpty
    private List<String> permissionIds;
}
