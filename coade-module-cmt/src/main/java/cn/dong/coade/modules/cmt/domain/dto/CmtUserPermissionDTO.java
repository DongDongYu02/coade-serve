package cn.dong.coade.modules.cmt.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "CMT用户权限授权 DTO")
public class CmtUserPermissionDTO {

    @Schema(description = "CMT用户 ID",hidden = true)
    private String cmtUserId;

    @Schema(description = "权限列表")
    @NotEmpty
    private List<String> permissions;
}
