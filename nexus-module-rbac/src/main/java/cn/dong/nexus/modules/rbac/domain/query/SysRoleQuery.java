package cn.dong.nexus.modules.rbac.domain.query;

import cn.dong.nexus.core.annotations.Query;
import cn.dong.nexus.core.base.PageQuery;
import cn.dong.nexus.modules.rbac.domain.entity.SysRole;
import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "角色管理 查询对象")
public class SysRoleQuery extends PageQuery<SysRole> {

    @Query(SqlKeyword.LIKE)
    @Schema(description = "权限名称")
    private String name;

    @Query(SqlKeyword.LIKE)
    @Schema(description = "角色编码")
    private String code;

    @Query(SqlKeyword.EQ)
    @Schema(description = "状态 0启用 1禁用")
    private Integer status;
}
