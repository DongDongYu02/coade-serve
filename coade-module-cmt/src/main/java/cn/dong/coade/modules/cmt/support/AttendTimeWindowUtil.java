package cn.dong.coade.modules.cmt.support;

import cn.dong.coade.modules.cmt.domain.bo.EkpAttendBusinessBO;
import cn.dong.coade.modules.cmt.domain.vo.UserAttendRecordVO;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 考勤时间窗口工具。
 *
 * 统一处理：
 * 1. 时间区间求交集；
 * 2. 业务记录按天过滤；
 * 3. 业务记录落在工作窗口内的分钟数计算；
 * 4. 打卡记录按天过滤。
 */
public final class AttendTimeWindowUtil {

    private AttendTimeWindowUtil() {
    }

    /**
     * 求两个时间区间的交集分钟数。
     *
     * 统一按 [start, end) 处理。
     */
    public static long intersectionMinutes(LocalDateTime range1Start,
                                           LocalDateTime range1End,
                                           LocalDateTime range2Start,
                                           LocalDateTime range2End) {
        if (range1Start == null || range1End == null || range2Start == null || range2End == null) {
            return 0L;
        }

        LocalDateTime actualStart = range1Start.isAfter(range2Start) ? range1Start : range2Start;
        LocalDateTime actualEnd = range1End.isBefore(range2End) ? range1End : range2End;

        return actualEnd.isAfter(actualStart)
                ? Duration.between(actualStart, actualEnd).toMinutes()
                : 0L;
    }

    /**
     * 业务记录是否与当天有交集。
     */
    public static boolean overlapsDay(EkpAttendBusinessBO item, LocalDate day) {
        if (item == null || item.getStartTime() == null || item.getEndTime() == null || day == null) {
            return false;
        }

        LocalDateTime dayBegin = day.atStartOfDay();
        LocalDateTime dayEnd = LocalDateTime.of(day, LocalTime.of(23, 59, 59));

        return !item.getEndTime().isBefore(dayBegin) && !item.getStartTime().isAfter(dayEnd);
    }

    /**
     * 业务记录按天过滤。
     */
    public static List<EkpAttendBusinessBO> filterBizByDay(List<EkpAttendBusinessBO> source, LocalDate day) {
        if (CollUtil.isEmpty(source)) {
            return List.of();
        }

        return source.stream()
                .filter(Objects::nonNull)
                .filter(item -> overlapsDay(item, day))
                .collect(Collectors.toList());
    }

    /**
     * 计算业务记录落在指定日期工作窗口内的分钟数。
     */
    public static long calculateBizMinutesInWorkRanges(List<EkpAttendBusinessBO> bizInfos,
                                                       String[][] workRanges,
                                                       LocalDate day) {
        if (CollUtil.isEmpty(bizInfos) || workRanges == null || workRanges.length == 0 || day == null) {
            return 0L;
        }

        long totalMinutes = 0L;

        for (EkpAttendBusinessBO biz : bizInfos) {
            if (biz == null || biz.getStartTime() == null || biz.getEndTime() == null) {
                continue;
            }

            for (String[] range : workRanges) {
                if (range == null || range.length < 2 || StrUtil.isBlank(range[0]) || StrUtil.isBlank(range[1])) {
                    continue;
                }

                LocalDateTime workStart = LocalDateTime.of(day, LocalTime.parse(range[0]));
                LocalDateTime workEnd = LocalDateTime.of(day, LocalTime.parse(range[1]));

                totalMinutes += intersectionMinutes(
                        biz.getStartTime(),
                        biz.getEndTime(),
                        workStart,
                        workEnd
                );
            }
        }

        return totalMinutes;
    }

    /**
     * 打卡记录是否属于当天。
     */
    public static boolean isRecordInDay(UserAttendRecordVO record, LocalDate day) {
        if (record == null || day == null || StrUtil.isBlank(record.getCheckinTime())) {
            return false;
        }

        return LocalDateTimeUtil.parse(record.getCheckinTime(), "yyyy-MM-dd HH:mm")
                .toLocalDate()
                .equals(day);
    }
}
