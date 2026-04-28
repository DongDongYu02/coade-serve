package cn.dong.coade.modules.cmt.support;

import cn.dong.coade.modules.cmt.domain.bo.AttendRuleBO;
import cn.dong.coade.modules.cmt.domain.enums.AttendRuleType;
import cn.hutool.core.util.ArrayUtil;

import java.time.Duration;
import java.time.LocalTime;

/**
 * 考勤规则时间窗口解析器。
 *
 * 统一管理不同业务场景下使用哪一套规则窗口：
 * 1. 打卡结果计算：使用规则原始 timeRanges；
 * 2. 请假/外出/出差/出勤天数计算：IMD 使用实际工作区间；
 * 3. 加班计算：IMD 使用实际工作区间。
 */
public final class AttendRuleWindowResolver {

    private AttendRuleWindowResolver() {
    }

    /**
     * 注塑部实际工作区间。
     *
     * 注意：这不是原始打卡点规则，而是用于请假、出勤、加班等时长计算的工作窗口。
     */
    private static final String[][] IMD_WORK_RANGES = {
            {"08:00", "11:15"},
            {"11:45", "17:30"},
            {"18:00", "20:30"}
    };

    /**
     * 打卡记录计算使用的窗口。
     */
    public static String[][] resolveAttendRecordRanges(AttendRuleBO rule) {
        if (rule == null) {
            return null;
        }
        if (ArrayUtil.isNotEmpty(rule.getTimeRanges())) {
            return rule.getTimeRanges();
        }
        if (rule.getRuleType() != null) {
            return rule.getRuleType().getRule();
        }
        return null;
    }

    /**
     * 请假、外出、出差、出勤天数计算使用的工作窗口。
     */
    public static String[][] resolveWorkCalcRanges(AttendRuleBO rule) {
        if (rule == null || rule.getRuleType() == null || rule.getRuleType() == AttendRuleType.EMPTY) {
            return new String[0][];
        }

        if (rule.getRuleType() == AttendRuleType.IMD) {
            return IMD_WORK_RANGES;
        }

        return rule.getTimeRanges() == null ? new String[0][] : rule.getTimeRanges();
    }

    /**
     * 加班计算使用的窗口。
     */
    public static String[][] resolveOvertimeCalcRanges(AttendRuleBO rule) {
        return resolveWorkCalcRanges(rule);
    }

    /**
     * 标准工作分钟数。
     */
    public static long standardWorkMinutes(AttendRuleBO rule) {
        return calculateRuleMinutes(resolveWorkCalcRanges(rule));
    }

    /**
     * 计算规则窗口分钟数。
     */
    public static long calculateRuleMinutes(String[][] ranges) {
        if (ranges == null || ranges.length == 0) {
            return 0L;
        }

        long minutes = 0L;
        for (String[] range : ranges) {
            if (range == null || range.length < 2) {
                continue;
            }

            LocalTime start = LocalTime.parse(range[0]);
            LocalTime end = LocalTime.parse(range[1]);

            if (end.isAfter(start)) {
                minutes += Duration.between(start, end).toMinutes();
            }
        }

        return minutes;
    }
}
