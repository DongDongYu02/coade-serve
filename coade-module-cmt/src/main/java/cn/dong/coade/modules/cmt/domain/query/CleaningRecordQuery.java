package cn.dong.coade.modules.cmt.domain.query;

import cn.dong.coade.modules.cmt.domain.entity.CmtCleaningRecord;
import cn.dong.nexus.core.annotations.Query;
import cn.dong.nexus.core.base.PageQuery;
import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "打扫记录查询对象")
public class CleaningRecordQuery extends PageQuery<CmtCleaningRecord> {

    @Schema(description = "区域ID")
    @Query(SqlKeyword.EQ)
    private String areaId;

    @Schema(description = "负责人ID")
    @Query(SqlKeyword.EQ)
    private String principalUserId;
}
