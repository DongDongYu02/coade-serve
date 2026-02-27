package cn.dong.nexus.modules.rbac.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "权限状态更新 DTO")
public class PermissionUpdateStatusDTO {

    @Schema(description = "权限 ID")
    @NotBlank
    private String id;

    @Schema(description = "权限状态")
    @NotNull
    private Integer status;
}
