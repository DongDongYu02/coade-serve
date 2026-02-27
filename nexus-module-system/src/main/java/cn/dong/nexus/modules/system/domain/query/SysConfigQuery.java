package cn.dong.nexus.modules.system.domain.query;

import cn.dong.nexus.core.annotations.Query;
import cn.dong.nexus.core.base.BaseQuery;
import cn.dong.nexus.modules.system.domain.entity.SysConfig;
import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "数据字典 查询对象")
public class SysConfigQuery extends BaseQuery<SysConfig> {

    @Schema(description = "配置键")
    @Query(SqlKeyword.EQ)
    private String key;


    @Schema(description = "配置分组")
    @Query(SqlKeyword.EQ)
    private Integer group;
}
