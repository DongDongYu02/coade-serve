package cn.dong.coade.modules.cmt.domain.query;

import cn.dong.coade.modules.cmt.domain.entity.Cmt6sReview;
import cn.dong.coade.modules.cmt.domain.entity.Cmt6sReviewProblem;
import cn.dong.nexus.core.annotations.Query;
import cn.dong.nexus.core.base.BaseEntity;
import cn.dong.nexus.core.base.PageQuery;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Objects;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "6S评审问题查询对象")
public class Cmt6sReviewProblemQuery extends PageQuery<Cmt6sReviewProblem> {

    @Schema(description = "部门")
    private String deptId;

    @Schema(description = "负责人")
    private String responsiblePersonId;

    @Schema(description = "状态 0分析中 1分析完成 2待整改 3已完成")
    private Integer status;

    @Schema(description = "创建时间范围 yyyy-MM-dd HH:mm  ")
    private String createTimeRange;

    @Schema(hidden = true)
    @Query(SqlKeyword.DESC)
    private String reviewId;

    @Override
    public QueryWrapper<Cmt6sReviewProblem> toQueryWrapper() {
        QueryWrapper<Cmt6sReviewProblem> queryWrapper = super.toQueryWrapper();

        boolean needFilterReview = StrUtil.isNotBlank(deptId)
                || StrUtil.isNotBlank(responsiblePersonId)
                || StrUtil.isNotBlank(createTimeRange)
                || Objects.nonNull(status);

        if (!needFilterReview) {
            return queryWrapper;
        }

        LambdaQueryChainWrapper<Cmt6sReview> reviewQueryWrapper = Db.lambdaQuery(Cmt6sReview.class)
                .select(Cmt6sReview::getId)
                .eq(StrUtil.isNotBlank(deptId), Cmt6sReview::getDeptId, deptId)
                .eq(StrUtil.isNotBlank(responsiblePersonId), Cmt6sReview::getResponsiblePersonId, responsiblePersonId)
                .eq(Objects.nonNull(status), Cmt6sReview::getStatus, status);
        if (StrUtil.isNotBlank(createTimeRange)) {
            String[] range = createTimeRange.split(",");
            reviewQueryWrapper.between(BaseEntity::getCreateTime, range[0] + " 00:00:00", range[1] + " 23:59:59");
        }
        List<Cmt6sReview> reviews = reviewQueryWrapper.list();

        if (reviews.isEmpty()) {
            this.setEmptyCondition(queryWrapper);
            return queryWrapper;
        }

        queryWrapper.lambda().in(Cmt6sReviewProblem::getReviewId, reviews.stream().map(Cmt6sReview::getId).toList());
        return queryWrapper;
    }
}
