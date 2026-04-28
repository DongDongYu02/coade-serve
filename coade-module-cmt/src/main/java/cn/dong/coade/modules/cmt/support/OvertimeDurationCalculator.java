package cn.dong.coade.modules.cmt.support;

import cn.dong.coade.modules.cmt.domain.bo.AttendDurationBO;
import cn.dong.coade.modules.cmt.domain.bo.AttendRuleBO;
import cn.dong.coade.modules.cmt.domain.enums.AttendRuleType;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * 加班时长计算器
 * <p>
 * 规则：
 * 1. 工作日加班：只算首段工时前 + 末段工时后，中间断档不算加班
 * 2. 休息日加班：算首段工时前 + 工时内 + 末段工时后，中间断档不算加班
 * 3. 当天规则为 EMPTY 时，取前一天规则作为参照规则
 * 4. IMD（注塑部）不直接使用原始打卡点规则，统一按专属加班计算窗口处理
 * 5. 最终总分钟按半小时取整
 */
@RequiredArgsConstructor
public class OvertimeDurationCalculator {

    /**
     * 注塑部加班计算窗口
     */
    private static final String[][] IMD_OVERTIME_RANGES = {
            {"08:00", "11:15"},
            {"11:45", "17:30"},
            {"18:00", "20:30"}
    };

    /**
     * 规则提供器
     * 你可以直接用 attendRuleService::getUserAttendRule 传进来
     */
    private final AttendRuleProvider attendRuleProvider;

    /**
     * 计算加班时长
     */
    public AttendDurationBO calculateDurationOfOvertime(String weComId, LocalDateTime beginTime, LocalDateTime endTime, Set<LocalDate> noNeedCheckinDates) {
        if (StrUtil.isBlank(weComId) || beginTime == null || endTime == null || !endTime.isAfter(beginTime)) {
            return buildAttendDurationBO(BigDecimal.ZERO, "0小时");
        }

        long totalMinutes = 0L;
        List<Long> dayStandardMinutesList = new ArrayList<>();

        LocalDate currentDate = beginTime.toLocalDate();
        LocalDate endDate = endTime.toLocalDate();

        while (!currentDate.isAfter(endDate)) {
            LocalDateTime currentDayBegin = currentDate.equals(beginTime.toLocalDate())
                    ? beginTime
                    : currentDate.atStartOfDay();

            LocalDateTime currentDayEnd = currentDate.equals(endDate)
                    ? endTime
                    : currentDate.plusDays(1).atStartOfDay();

            if (!currentDayEnd.isAfter(currentDayBegin)) {
                currentDate = currentDate.plusDays(1);
                continue;
            }

            AttendRuleBO currentRule = getRule(weComId, currentDate);

            boolean noNeedCheckinDate = noNeedCheckinDates.contains(currentDate);
            boolean emptyRule = currentRule == null || currentRule.getRuleType() == AttendRuleType.EMPTY;

            // 无需打卡日期，也按休息日计算加班
            boolean restDay = noNeedCheckinDate || emptyRule;

            AttendRuleBO calcRule;

            if (noNeedCheckinDate && !emptyRule) {
                // 关键点：
                // 当天原本是正常工作日，只是被配置为无需打卡，
                // 加班计算仍然使用当天原本规则作为参照。
                calcRule = currentRule;
            } else if (restDay) {
                // 原本就是休息日，取前一天规则作为参照
                calcRule = getRule(weComId, currentDate.minusDays(1));
            } else {
                // 正常工作日
                calcRule = currentRule;
            }

            List<TimeWindow> calcWindows = getOvertimeCalcWindows(calcRule);

            // 如果取不到规则，兜底整段都算加班
            if (calcWindows.isEmpty()) {
                totalMinutes += Duration.between(currentDayBegin, currentDayEnd).toMinutes();
                currentDate = currentDate.plusDays(1);
                continue;
            }

            long currentDayStandardMinutes = calculateWindowsMinutes(calcWindows);
            if (currentDayStandardMinutes > 0) {
                dayStandardMinutesList.add(currentDayStandardMinutes);
            }

            long currentDayMinutes = restDay
                    ? calculateRestDayOvertimeMinutes(currentDate, currentDayBegin, currentDayEnd, calcWindows)
                    : calculateWorkDayOvertimeMinutes(currentDate, currentDayBegin, currentDayEnd, calcWindows);

            totalMinutes += currentDayMinutes;
            currentDate = currentDate.plusDays(1);
        }

        // 总分钟统一按半小时取整
        long roundedMinutes = roundOvertimeMinutes(totalMinutes);

        BigDecimal duration = BigDecimal.valueOf(roundedMinutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP)
                .stripTrailingZeros();

        String durationFormat = formatAttendDuration(roundedMinutes, dayStandardMinutesList);

        return buildAttendDurationBO(duration, durationFormat);
    }

    /**
     * 工作日加班：
     * 只算首段工时前 + 末段工时后
     */
    private long calculateWorkDayOvertimeMinutes(LocalDate currentDate,
                                                 LocalDateTime currentDayBegin,
                                                 LocalDateTime currentDayEnd,
                                                 List<TimeWindow> calcWindows) {
        if (calcWindows.isEmpty()) {
            return 0L;
        }

        TimeWindow firstWindow = calcWindows.get(0);
        TimeWindow lastWindow = calcWindows.get(calcWindows.size() - 1);

        long beforeMinutes = intersectionMinutes(
                currentDayBegin,
                currentDayEnd,
                LocalDateTime.of(currentDate, LocalTime.MIN),
                LocalDateTime.of(currentDate, firstWindow.getStart())
        );

        long afterMinutes = intersectionMinutes(
                currentDayBegin,
                currentDayEnd,
                LocalDateTime.of(currentDate, lastWindow.getEnd()),
                currentDate.plusDays(1).atStartOfDay()
        );

        return beforeMinutes + afterMinutes;
    }

    /**
     * 休息日加班：
     * 算首段工时前 + 工时内 + 末段工时后
     * 中间断档不算加班
     */
    private long calculateRestDayOvertimeMinutes(LocalDate currentDate,
                                                 LocalDateTime currentDayBegin,
                                                 LocalDateTime currentDayEnd,
                                                 List<TimeWindow> calcWindows) {
        if (calcWindows.isEmpty()) {
            return 0L;
        }

        TimeWindow firstWindow = calcWindows.get(0);
        TimeWindow lastWindow = calcWindows.get(calcWindows.size() - 1);

        long beforeMinutes = intersectionMinutes(
                currentDayBegin,
                currentDayEnd,
                LocalDateTime.of(currentDate, LocalTime.MIN),
                LocalDateTime.of(currentDate, firstWindow.getStart())
        );

        long inWorkMinutes = 0L;
        for (TimeWindow window : calcWindows) {
            inWorkMinutes += intersectionMinutes(
                    currentDayBegin,
                    currentDayEnd,
                    LocalDateTime.of(currentDate, window.getStart()),
                    LocalDateTime.of(currentDate, window.getEnd())
            );
        }

        long afterMinutes = intersectionMinutes(
                currentDayBegin,
                currentDayEnd,
                LocalDateTime.of(currentDate, lastWindow.getEnd()),
                currentDate.plusDays(1).atStartOfDay()
        );

        return beforeMinutes + inWorkMinutes + afterMinutes;
    }

    /**
     * 获取加班计算窗口
     */
    private List<TimeWindow> getOvertimeCalcWindows(AttendRuleBO rule) {
        return parseTimeWindows(AttendRuleWindowResolver.resolveOvertimeCalcRanges(rule));
    }

    /**
     * 解析时间窗口并按开始时间排序
     */
    private List<TimeWindow> parseTimeWindows(String[][] ranges) {
        List<TimeWindow> windows = new ArrayList<>();
        if (ranges == null || ranges.length == 0) {
            return windows;
        }

        Arrays.stream(ranges)
                .filter(Objects::nonNull)
                .filter(item -> item.length >= 2)
                .filter(item -> StrUtil.isNotBlank(item[0]) && StrUtil.isNotBlank(item[1]))
                .forEach(item -> {
                    LocalTime start = LocalTime.parse(item[0]);
                    LocalTime end = LocalTime.parse(item[1]);
                    if (end.isAfter(start)) {
                        windows.add(new TimeWindow(start, end));
                    }
                });

        windows.sort(Comparator.comparing(TimeWindow::getStart));
        return windows;
    }

    /**
     * 计算所有窗口的总分钟
     */
    private long calculateWindowsMinutes(List<TimeWindow> windows) {
        long minutes = 0L;
        for (TimeWindow window : windows) {
            minutes += Duration.between(window.getStart(), window.getEnd()).toMinutes();
        }
        return minutes;
    }

    /**
     * 求两个时间区间交集分钟数
     * 统一按 [start, end) 处理
     */
    private long intersectionMinutes(LocalDateTime range1Start,
                                     LocalDateTime range1End,
                                     LocalDateTime range2Start,
                                     LocalDateTime range2End) {
        return AttendTimeWindowUtil.intersectionMinutes(range1Start, range1End, range2Start, range2End);
    }

    /**
     * 加班按半小时取整
     * 00~14 分 -> 舍去
     * 15~44 分 -> 记 30 分
     * 45~59 分 -> 进 1 小时
     */
    private long roundOvertimeMinutes(long minutes) {
        if (minutes <= 0) {
            return 0L;
        }

        long hourPart = minutes / 60;
        long minutePart = minutes % 60;

        if (minutePart <= 14) {
            return hourPart * 60;
        }
        if (minutePart <= 44) {
            return hourPart * 60 + 30;
        }
        return (hourPart + 1) * 60;
    }

    /**
     * 格式化时长：
     * 只有超过一天才显示“天”
     * 一天按每天规则工时折算
     */
    private String formatAttendDuration(long totalMinutes, List<Long> dayStandardMinutesList) {
        if (totalMinutes <= 0) {
            return "0小时";
        }

        if (dayStandardMinutesList == null || dayStandardMinutesList.isEmpty()) {
            return formatHours(totalMinutes);
        }

        long remainingMinutes = totalMinutes;
        int days = 0;

        for (Long standardMinutes : dayStandardMinutesList) {
            if (standardMinutes == null || standardMinutes <= 0) {
                continue;
            }
            if (remainingMinutes > standardMinutes) {
                remainingMinutes -= standardMinutes;
                days++;
            } else {
                break;
            }
        }

        if (days == 0) {
            return formatHours(totalMinutes);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(days).append("天");
        if (remainingMinutes > 0) {
            sb.append(formatHoursWithoutUnit(remainingMinutes)).append("小时");
        }
        return sb.toString();
    }

    private String formatHours(long minutes) {
        return formatHoursWithoutUnit(minutes) + "小时";
    }

    private String formatHoursWithoutUnit(long minutes) {
        return AttendDurationFormatter.toHourNumber(minutes);
    }

    private AttendDurationBO buildAttendDurationBO(BigDecimal duration, String durationFormat) {
        AttendDurationBO bo = new AttendDurationBO();
        bo.setDuration(duration);
        bo.setDurationFormat(durationFormat);
        return bo;
    }

    private AttendRuleBO getRule(String weComId, LocalDate date) {
        return attendRuleProvider.getUserAttendRule(weComId, date);
    }

    /**
     * 规则提供器
     */
    @FunctionalInterface
    public interface AttendRuleProvider {
        AttendRuleBO getUserAttendRule(String weComId, LocalDate date);
    }

    /**
     * 时间窗口
     */
    private static class TimeWindow {
        private final LocalTime start;
        private final LocalTime end;

        public TimeWindow(LocalTime start, LocalTime end) {
            this.start = start;
            this.end = end;
        }

        public LocalTime getStart() {
            return start;
        }

        public LocalTime getEnd() {
            return end;
        }
    }
}