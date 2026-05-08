package cn.dong.coade.modules.cmt.domain.query;

import cn.dong.coade.modules.cmt.domain.entity.CmtCleaningArea;
import cn.dong.nexus.core.annotations.Query;
import cn.dong.nexus.core.base.PageQuery;
import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CleaningAreaQuery extends PageQuery<CmtCleaningArea> {

    @Schema(description = "区域名称")
    @Query(SqlKeyword.LIKE)
    private String areaName;

    @Schema(description = "负责人")
    @Query(SqlKeyword.EQ)
    private String principalUserId;
}
