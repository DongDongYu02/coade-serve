package cn.dong.coade.modules.cmt.domain.query;

import cn.dong.coade.modules.cmt.constants.CmtLocalConstants;
import cn.dong.nexus.core.exception.BizException;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

@Data
@Schema(description = "问题/需求统计分析查询对象")
public class IssueDemandAnalysisQuery {
    @Schema(description = "时间范围 1本年度 2本季度 3本月 4本周")
    @NotNull
    private Integer timeRange;

    public LocalDateTime[] buildTimeRange() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start;
        LocalDateTime end;

        switch (timeRange) {
            case CmtLocalConstants.ANALYSIS_TIME_RANGE.THIS_YEAR: // 本年度
                start = now.withDayOfYear(1)
                        .withHour(0)
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0);
                end = start.plusYears(1);
                break;

            case CmtLocalConstants.ANALYSIS_TIME_RANGE.THIS_QUARTER: // 本季度
                int currentMonth = now.getMonthValue();
                int currentQuarterStartMonth = ((currentMonth - 1) / 3) * 3 + 1;

                start = now.withMonth(currentQuarterStartMonth)
                        .withDayOfMonth(1)
                        .withHour(0)
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0);
                end = start.plusMonths(3);
                break;

            case CmtLocalConstants.ANALYSIS_TIME_RANGE.THIS_MONTH: // 本月
                start = now.withDayOfMonth(1)
                        .withHour(0)
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0);
                end = start.plusMonths(1);
                break;

            case CmtLocalConstants.ANALYSIS_TIME_RANGE.THIS_WEEK: // 本周，周一到下周一
                start = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                        .withHour(0)
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0);
                end = start.plusWeeks(1);
                break;

            case CmtLocalConstants.ANALYSIS_TIME_RANGE.LAST_QUARTER: // 上季度
                int month = now.getMonthValue();
                int thisQuarterStartMonth = ((month - 1) / 3) * 3 + 1;

                end = now.withMonth(thisQuarterStartMonth)
                        .withDayOfMonth(1)
                        .withHour(0)
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0);
                start = end.minusMonths(3);
                break;

            case CmtLocalConstants.ANALYSIS_TIME_RANGE.LAST_MONTH: // 上月
                end = now.withDayOfMonth(1)
                        .withHour(0)
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0);
                start = end.minusMonths(1);
                break;

            case CmtLocalConstants.ANALYSIS_TIME_RANGE.LAST_WEEK: // 上周，上一周周一到本周周一
                end = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                        .withHour(0)
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0);
                start = end.minusWeeks(1);
                break;

            default:
                throw new BizException("无效的时间范围");
        }

        return new LocalDateTime[]{start, end};
    }


}
