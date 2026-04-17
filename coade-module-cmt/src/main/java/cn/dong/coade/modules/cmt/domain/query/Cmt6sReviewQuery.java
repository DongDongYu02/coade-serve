package cn.dong.coade.modules.cmt.domain.query;

import cn.dong.coade.modules.cmt.domain.entity.Cmt6sReview;
import cn.dong.nexus.core.annotations.Query;
import cn.dong.nexus.core.base.BaseEntity;
import cn.dong.nexus.core.base.PageQuery;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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

    @Schema(description = "部门")
    @Query(SqlKeyword.EQ)
    private String deptId;

    @Schema(description = "负责人")
    @Query(SqlKeyword.EQ)
    private String responsiblePersonId;

    @Schema(description = "创建时间范围 yyyy-MM-dd HH:mm  ")
    private String createTimeRange;

    @Schema(hidden = true)
    @Query(SqlKeyword.DESC)
    private LocalDateTime createTime;

    @Override
    public QueryWrapper<Cmt6sReview> toQueryWrapper() {
        QueryWrapper<Cmt6sReview> queryWrapper = super.toQueryWrapper();
        if (StrUtil.isNotBlank(createTimeRange)) {
            String[] range = createTimeRange.split(",");
            queryWrapper.lambda().between(BaseEntity::getCreateTime, range[0] + " 00:00:00", range[1] + " 23:59:59");
        }
        return queryWrapper;
    }
}
