package cn.dong.coade.modules.cmt.domain.query;

import cn.dong.coade.modules.cmt.domain.entity.Cmt6sReview;
import cn.dong.nexus.core.annotations.Query;
import cn.dong.nexus.core.base.PageQuery;
import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "6S评审记录查询对象")
public class Cmt6sReviewQuery extends PageQuery<Cmt6sReview> {

    @Schema(description = "状态 0分析中 1分析完成 2待整改 3已完成")
    @Query(SqlKeyword.EQ)
    private Integer status;

    @Schema(hidden = true)
    @Query(SqlKeyword.DESC)
    private LocalDateTime createTime;

}
