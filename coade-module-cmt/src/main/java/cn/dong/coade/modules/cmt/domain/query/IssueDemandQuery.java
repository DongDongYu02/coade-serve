package cn.dong.coade.modules.cmt.domain.query;

import cn.dong.coade.modules.cmt.domain.entity.CmtIssueDemand;
import cn.dong.nexus.core.annotations.Query;
import cn.dong.nexus.core.base.PageQuery;
import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "CMT用户 列表查询对象")
public class IssueDemandQuery extends PageQuery<CmtIssueDemand> {

    @Schema(description = "类型")
    @Query(SqlKeyword.EQ)
    private Integer type;

    @Schema(description = "状态 ")
    @Query(SqlKeyword.EQ)
    private Integer status;

}
