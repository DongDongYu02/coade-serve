package cn.dong.nexus.modules.rbac.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "修改密码 DTO")
public class ChangePasswordDTO {

    @NotBlank
    @Schema(description = "旧密码")
    private String oldPassword;

    @NotBlank
    @Schema(description = "新密码")
    private String newPassword;
}
