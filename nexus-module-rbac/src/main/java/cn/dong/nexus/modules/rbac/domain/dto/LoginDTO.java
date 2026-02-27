package cn.dong.nexus.modules.rbac.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDTO {

    @NotBlank
    @Schema(description = "用户名")
    private String username;

    @NotBlank
    @Schema(description = "密码")
    private String password;
}
