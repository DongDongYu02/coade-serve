package cn.dong.nexus.modules.rbac.domain.vo;

import cn.dong.nexus.common.constants.GlobalConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

@Data
@Schema(description = "角色管理 VO")
public class SysRoleVO {

    @Schema(description = "角色 ID")
    private String id;

    @Schema(description = "权限名称")
    private String name;

    @Schema(description = "角色编码")
    private String code;

    @Schema(description = "角色描述")
    private String description;

    @Schema(description = "状态 0启用 1禁用")
    private Integer status;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = GlobalConstants.DatePattern.NORMAL, timezone = GlobalConstants.ZoneTime.GMT8)
    private LocalDateTime createTime;
}
