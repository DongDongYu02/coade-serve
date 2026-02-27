package cn.dong.nexus.modules.rbac.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "用户角色 DTO")
public class UserRoleDTO {

    private String userId;

    private List<String> roleIds;

    private List<String> roleNames;
}
