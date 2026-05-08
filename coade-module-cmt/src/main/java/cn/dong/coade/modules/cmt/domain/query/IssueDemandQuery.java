package cn.dong.coade.modules.cmt.domain.query;

import cn.dong.coade.modules.cmt.constants.CmtLocalConstants;
import cn.dong.coade.modules.cmt.domain.entity.CmtIssueDemand;
import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.core.annotations.Query;
import cn.dong.nexus.core.base.PageQuery;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "问题需求 列表查询对象")
public class IssueDemandQuery extends PageQuery<CmtIssueDemand> {

    @Schema(description = "类型")
    @Query(SqlKeyword.EQ)
    private Integer type;

    @Schema(description = "系统类型")
    @Query(SqlKeyword.EQ)
    private Integer systemType;

    @Schema(description = "状态")
    @Query(SqlKeyword.IN)
    private String status;

    @Schema(description = "部门")
    @Query(SqlKeyword.LIKE)
    private String proposeDept;

    @Schema(hidden = true)
    @Query(SqlKeyword.DESC)
    private LocalDateTime createTime;

    @Schema(description = "是否逾期")
    private Integer isOverdue;

    @Schema(description = "只看自己的")
    private Integer onlyProposer;

    @Schema(description = "是否紧急")
    @Query(SqlKeyword.EQ)
    private Integer isUrgent;


    @Override
    public QueryWrapper<CmtIssueDemand> toQueryWrapper() {
        QueryWrapper<CmtIssueDemand> queryWrapper = super.toQueryWrapper();
        if (Objects.nonNull(isOverdue)) {
            LocalDate today = LocalDate.now();
            List<Integer> finishedStatuses = List.of(
                    CmtLocalConstants.ISSUE_DEMAND_STATUS.COMPLETED,
                    CmtLocalConstants.ISSUE_DEMAND_STATUS.REJECTED,
                    CmtLocalConstants.ISSUE_DEMAND_STATUS.VOIDED
            );

            if (GlobalConstants.INT_YES.equals(isOverdue)) {
                queryWrapper.lambda()
                        .isNotNull(CmtIssueDemand::getPlanFinishTime)
                        .and(wrapper -> wrapper
                                .and(w -> w
                                        .isNotNull(CmtIssueDemand::getActualFinishTime)
                                        .apply("actual_finish_time > plan_finish_time"))
                                .or(w -> w
                                        .isNull(CmtIssueDemand::getActualFinishTime)
                                        .notIn(CmtIssueDemand::getStatus, finishedStatuses)
                                        .lt(CmtIssueDemand::getPlanFinishTime, today))
                        );
            }

            if (GlobalConstants.INT_NO.equals(isOverdue)) {
                queryWrapper.lambda()
                        .and(wrapper -> wrapper
                                .isNull(CmtIssueDemand::getPlanFinishTime)
                                .or(w -> w
                                        .isNotNull(CmtIssueDemand::getActualFinishTime)
                                        .apply("actual_finish_time <= plan_finish_time"))
                                .or(w -> w
                                        .isNull(CmtIssueDemand::getActualFinishTime)
                                        .in(CmtIssueDemand::getStatus, finishedStatuses))
                                .or(w -> w
                                        .isNull(CmtIssueDemand::getActualFinishTime)
                                        .ge(CmtIssueDemand::getPlanFinishTime, today))
                        );
            }
        }
        return queryWrapper;
    }
}
