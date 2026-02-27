package cn.dong.nexus.modules.rbac.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "权限树选择 VO")
public class PermissionSelectionVO {

    @Schema(description = "权限 ID")
    private Long id;

    @Schema(description = "权限名称")
    private String name;

    @Schema(description = "权限类型")
    private Integer type;

    @Schema(description = "子权限列表")
    private List<PermissionSelectionVO> children;

}
