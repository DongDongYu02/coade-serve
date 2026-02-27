package cn.dong.nexus.modules.system.domain.query;

import cn.dong.nexus.core.annotations.Query;
import cn.dong.nexus.core.base.PageQuery;
import cn.dong.nexus.modules.system.domain.entity.SysDataDict;
import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "数据字典 查询对象")
public class SysDataDictQuery extends PageQuery<SysDataDict> {

    @Query(SqlKeyword.LIKE)
    @Schema(description = "字典名称")
    private String name;

    @Query(SqlKeyword.LIKE)
    @Schema(description = "字典编码")
    private String code;

    @Query(SqlKeyword.ASC)
    @Schema(description = "排序")
    private Integer sort;
}
