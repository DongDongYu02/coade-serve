package cn.dong.nexus.modules.rbac.domain.query;

import cn.dong.nexus.core.annotations.Query;
import cn.dong.nexus.core.base.BaseQuery;
import cn.dong.nexus.modules.rbac.domain.entity.SysPermission;
import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "权限管理 查询对象")
public class SysPermissionQuery extends BaseQuery<SysPermission> {

    @Query(SqlKeyword.LIKE)
    @Schema(description = "权限名称")
    private String name;

    @Query(SqlKeyword.ASC)
    @Schema(hidden = true)
    private String sort;
}
