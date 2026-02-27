package cn.dong.nexus.modules.rbac.domain.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class PermissionTreeSelectionQuery {

    @Schema(description = "包含的权限类型")
    @NotEmpty
    private List<Integer> includeTypes;
}
