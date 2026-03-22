package cn.dong.coade.modules.cmt.utils;

import cn.dong.coade.modules.cmt.domain.bo.EkpAttendBusinessBO;
import cn.dong.coade.modules.cmt.domain.bo.EkpAttendRuleBO;
import cn.dong.coade.modules.cmt.domain.vo.UserAttendRecordVO;
import cn.dong.coade.modules.cmt.domain.vo.UserLeaveAttendVO;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 考勤结果计算器
 *
 * 规则：
 * 1. 每个班次始终只有两个规则点：上班点、下班点
 * 2. 请假/外出/出差不会新增打卡点，只会影响原规则点
 * 3. 班次开始点被业务覆盖：
 *    - 覆盖整个班次：开始点显示业务状态
 *    - 只覆盖前半段：开始点顺延到业务结束时间，且只能匹配业务结束后的打卡
 * 4. 班次结束点被业务覆盖：
 *    - 如果业务从班次开始就覆盖到结束：结束点显示业务状态
 *    - 如果业务在班中开始并覆盖到结束：下班点前移到业务开始时间，且只能匹配该时刻及之前的打卡
 *    - 如果没有打到该下班点，仍然显示“下班缺卡”
 * 5. 中间下班点 -> 上班点之间允许重叠打卡：
 *    - 共享区间内打 2 次，则下班点取最早一条，上班点取最晚一条
 *    - 共享区间内只有 1 次，则只能命中一个点
 * 6. 同一班次的上班点 -> 下班点：
 *    - 下班点从本班次上班时间开始匹配，支持“14:30 打卡匹配 17:30 为早退”
 */
@Component
public class AttendRecordCalculator {

    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * 计算指定日期的考勤结果
     */
    public List<UserAttendRecordVO> calculate(LocalDate attendDate,
                                              List<UserAttendRecordVO> actualRecords,
                                              EkpAttendRuleBO rule,
                                              List<EkpAttendBusinessBO> leaveInfos,
                                              List<EkpAttendBusinessBO> outInfos,
                                              List<EkpAttendBusinessBO> tripInfos) {

        if (rule == null || ArrayUtil.isEmpty(rule.getTimeRanges())) {
            return sortRawRecords(actualRecords);
        }

        int weekDay = attendDate.getDayOfWeek().getValue(); // 1=周一 ... 7=周日
        if (ArrayUtil.isEmpty(rule.getWorkDays()) || !ArrayUtil.contains(rule.getWorkDays(), weekDay)) {
            return sortRawRecords(actualRecords);
        }

        List<BizWindow> bizWindows = buildBizWindows(leaveInfos, outInfos, tripInfos);

        // 固定规则点，只生成“每个班次的上班点/下班点”
        List<AttendPoint> points = buildAttendPoints(attendDate, rule, bizWindows);

        // 实际打卡
        List<ActualPunch> punches = CollUtil.emptyIfNull(actualRecords).stream()
                .filter(item -> StrUtil.isNotBlank(item.getCheckinTime()))
                .map(item -> new ActualPunch(
                        LocalDateTime.parse(item.getCheckinTime(), DATE_TIME_FMT),
                        StrUtil.blankToDefault(item.getLocation(), "-"),
                        item.getExceptionStatus(),
                        item.getIsReissue()
                ))
                .sorted(Comparator.comparing(ActualPunch::getTime))
                .collect(Collectors.toList());

        return matchPoints(points, punches, attendDate);
    }

    /**
     * 计算今日考勤
     */
    public List<UserAttendRecordVO> calculateToday(List<UserAttendRecordVO> actualRecords,
                                                   EkpAttendRuleBO rule,
                                                   List<EkpAttendBusinessBO> leaveInfos,
                                                   List<EkpAttendBusinessBO> outInfos,
                                                   List<EkpAttendBusinessBO> tripInfos) {
        return calculate(LocalDate.now(), actualRecords, rule, leaveInfos, outInfos, tripInfos);
    }

    /**
     * 构造规则点
     */
    private List<AttendPoint> buildAttendPoints(LocalDate attendDate,
                                                EkpAttendRuleBO rule,
                                                List<BizWindow> bizWindows) {
        List<AttendPoint> points = new ArrayList<>();

        for (String[] range : rule.getTimeRanges()) {
            LocalDateTime sessionStart = attendDate.atTime(LocalTime.parse(range[0], TIME_FMT));
            LocalDateTime sessionEnd = attendDate.atTime(LocalTime.parse(range[1], TIME_FMT));

            // 上班点
            AttendPoint onDutyPoint = buildOnDutyPoint(sessionStart, sessionEnd, bizWindows);
            points.add(onDutyPoint);

            // 下班点
            AttendPoint offDutyPoint = buildOffDutyPoint(sessionStart, sessionEnd, bizWindows);
            points.add(offDutyPoint);
        }

        points.sort(Comparator.comparing(AttendPoint::getExpectedTime)
                .thenComparing(p -> p.getType().ordinal()));
        return points;
    }

    /**
     * 构造上班点
     *
     * 规则：
     * - 如果业务覆盖了班次开始点：
     *   - 覆盖整个班次：开始点显示业务状态，时间仍为班次开始时间
     *   - 只覆盖前半段：开始点顺延到业务结束时间，且只能匹配业务结束后的打卡
     * - 否则：正常上班点
     */
    private AttendPoint buildOnDutyPoint(LocalDateTime sessionStart,
                                         LocalDateTime sessionEnd,
                                         List<BizWindow> bizWindows) {
        BizWindow bizAtStart = findBizCoveringStart(sessionStart, bizWindows);

        // 没有覆盖开始点
        if (bizAtStart == null) {
            return new AttendPoint(sessionStart, PunchType.ON_DUTY, null, null, null);
        }

        // 覆盖整个班次
        if (!bizAtStart.getEnd().isBefore(sessionEnd)) {
            return new AttendPoint(sessionStart, PunchType.ON_DUTY, bizAtStart.getStatus(), null, null);
        }

        // 只覆盖前半段：开始点顺延到业务结束，且只能匹配业务结束后的打卡
        return new AttendPoint(
                bizAtStart.getEnd(),
                PunchType.ON_DUTY,
                null,
                bizAtStart.getEnd(),
                null
        );
    }

    /**
     * 构造下班点
     *
     * 规则：
     * - 如果业务覆盖了班次结束点：
     *   - 业务从班次开始就覆盖到结束：结束点显示业务状态
     *   - 业务在班中开始并覆盖到结束：下班点前移到业务开始时间，且只能匹配业务开始前（含）的打卡
     * - 否则：正常下班点
     */
    private AttendPoint buildOffDutyPoint(LocalDateTime sessionStart,
                                          LocalDateTime sessionEnd,
                                          List<BizWindow> bizWindows) {
        BizWindow bizAtEnd = findBizCoveringEnd(sessionEnd, bizWindows);

        // 没有覆盖结束点，正常下班点
        if (bizAtEnd == null) {
            return new AttendPoint(sessionEnd, PunchType.OFF_DUTY, null, null, null);
        }

        // 业务从班次开始就覆盖到结束：整个班次都处于业务状态
        if (!bizAtEnd.getStart().isAfter(sessionStart)) {
            return new AttendPoint(sessionEnd, PunchType.OFF_DUTY, bizAtEnd.getStatus(), null, null);
        }

        // 业务在班中开始，并覆盖到下班点：
        // 下班点前移到业务开始时间，且只能匹配该时刻及之后的打卡
        return new AttendPoint(
                bizAtEnd.getStart(),
                PunchType.OFF_DUTY,
                null,
                bizAtEnd.getStart(),
                null
        );
    }

    /**
     * 匹配规则点与实际打卡
     */
    private List<UserAttendRecordVO> matchPoints(List<AttendPoint> points,
                                                 List<ActualPunch> punches,
                                                 LocalDate attendDate) {
        List<UserAttendRecordVO> result = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        boolean isToday = LocalDate.now().equals(attendDate);

        // 普通规则点匹配后占用，避免同一条打卡被重复匹配
        Set<Integer> usedPunchIndexes = new HashSet<>();

        for (int i = 0; i < points.size(); i++) {
            AttendPoint current = points.get(i);
            UserAttendRecordVO vo = new UserAttendRecordVO();

            LocalDateTime startBoundary = resolveStartBoundary(points, i, attendDate);
            LocalDateTime endBoundary = resolveEndBoundary(points, i, attendDate);

            List<Integer> candidateIndexes = new ArrayList<>();
            for (int idx = 0; idx < punches.size(); idx++) {
                if (usedPunchIndexes.contains(idx)) {
                    continue;
                }

                ActualPunch punch = punches.get(idx);

                // [startBoundary, endBoundary)
                if (punch.getTime().isBefore(startBoundary) || !punch.getTime().isBefore(endBoundary)) {
                    continue;
                }

                if (current.getMatchStartLimit() != null && punch.getTime().isBefore(current.getMatchStartLimit())) {
                    continue;
                }
                if (current.getMatchEndLimit() != null && punch.getTime().isAfter(current.getMatchEndLimit())) {
                    continue;
                }

                candidateIndexes.add(idx);
            }

            Integer matchedIndex = selectMatchedIndex(points, i, candidateIndexes, punches);
            ActualPunch matched = matchedIndex == null ? null : punches.get(matchedIndex);

            // 固定业务状态：请假 / 出差 / 外出
            if (StrUtil.isNotBlank(current.getFixedStatus())) {
                if (matched != null) {
                    vo.setCheckinTime(matched.getTime().format(DATE_TIME_FMT));
                    vo.setRuleCheckinTime(current.getExpectedTime().format(DATE_TIME_FMT));
                    vo.setLocation(matched.getLocation());
                    vo.setExceptionStatus(matched.getExceptionStatus());
                    vo.setIsReissue(matched.getIsReissue());
                } else {
                    vo.setCheckinTime(current.getExpectedTime().format(DATE_TIME_FMT));
                    vo.setRuleCheckinTime(current.getExpectedTime().format(DATE_TIME_FMT));
                    vo.setLocation("-");
                }
                vo.setStatus(current.getFixedStatus());
                result.add(vo);
                continue;
            }

            // 今天且规则点未到，只有在“还没匹配到实际打卡”时才显示待打卡
            if (isToday && current.getExpectedTime().isAfter(now) && matched == null) {
                vo.setCheckinTime(current.getExpectedTime().format(DATE_TIME_FMT));
                vo.setRuleCheckinTime(current.getExpectedTime().format(DATE_TIME_FMT));
                vo.setLocation("-");
                vo.setStatus("待打卡");
                result.add(vo);
                continue;
            }

            // 普通规则点
            if (matched == null) {
                vo.setCheckinTime(current.getExpectedTime().format(DATE_TIME_FMT));
                vo.setRuleCheckinTime(current.getExpectedTime().format(DATE_TIME_FMT));
                vo.setLocation("-");
                vo.setStatus(current.getType() == PunchType.ON_DUTY ? "上班缺卡" : "下班缺卡");
            } else {
                vo.setCheckinTime(matched.getTime().format(DATE_TIME_FMT));
                vo.setRuleCheckinTime(current.getExpectedTime().format(DATE_TIME_FMT));
                vo.setLocation(matched.getLocation());
                vo.setStatus(calcNormalStatus(current, matched.getTime()));
                vo.setExceptionStatus(matched.getExceptionStatus());
                vo.setIsReissue(matched.getIsReissue());
                // 普通规则点匹配成功后，占用这条打卡
                usedPunchIndexes.add(matchedIndex);
            }

            result.add(vo);
        }

        return result;
    }

    /**
     * 计算当前规则点的匹配开始边界
     */
    private LocalDateTime resolveStartBoundary(List<AttendPoint> points, int index, LocalDate attendDate) {
        if (index == 0) {
            return attendDate.atStartOfDay();
        }

        AttendPoint prev = points.get(index - 1);
        AttendPoint current = points.get(index);

        // 中间下班点 -> 上班点 共享窗口
        if (isOverlapPair(prev, current)) {
            return prev.getExpectedTime();
        }

        // 同一班次 上班点 -> 下班点
        // 下班点从本班次上班时间开始匹配，这样 14:30 才能匹配 17:30 为早退
        if (isSessionPair(prev, current)) {
            return prev.getExpectedTime();
        }

        return midpoint(prev.getExpectedTime(), current.getExpectedTime());
    }

    /**
     * 是否为同一班次的上下班点
     */
    private boolean isSessionPair(AttendPoint left, AttendPoint right) {
        if (left == null || right == null) {
            return false;
        }

        return left.getType() == PunchType.ON_DUTY
               && right.getType() == PunchType.OFF_DUTY
               && StrUtil.isBlank(left.getFixedStatus())
               && StrUtil.isBlank(right.getFixedStatus());
    }

    /**
     * 计算当前规则点的匹配结束边界
     */
    private LocalDateTime resolveEndBoundary(List<AttendPoint> points, int index, LocalDate attendDate) {
        if (index == points.size() - 1) {
            return attendDate.plusDays(1).atStartOfDay();
        }

        AttendPoint current = points.get(index);
        AttendPoint next = points.get(index + 1);

        // 中间下班点 -> 上班点 共享窗口：当前下班点匹配到后一个上班点时间结束
        if (isOverlapPair(current, next)) {
            return next.getExpectedTime();
        }

        return midpoint(current.getExpectedTime(), next.getExpectedTime());
    }

    /**
     * 是否为允许重叠匹配的中间点：下班点 -> 上班点
     */
    private boolean isOverlapPair(AttendPoint left, AttendPoint right) {
        if (left == null || right == null) {
            return false;
        }

        return left.getType() == PunchType.OFF_DUTY
               && right.getType() == PunchType.ON_DUTY
               && StrUtil.isBlank(left.getFixedStatus())
               && StrUtil.isBlank(right.getFixedStatus())
               // 业务顺延后的上班点，不参与中间重叠匹配
               && right.getMatchStartLimit() == null
               && !right.getExpectedTime().isBefore(left.getExpectedTime());
    }

    /**
     * 选择最终匹配的打卡记录
     *
     * 普通规则：
     * - 上班点：取最早一条
     * - 下班点：取最晚一条
     *
     * 重叠窗口特殊规则（下班点 -> 上班点）：
     * - 下班点：优先取共享区间内最早一条；若共享区间没卡，再回退取整个候选集最后一条
     * - 上班点：只取共享区间内最后一条；若共享区间没卡，不去吃后面班次的卡
     */
    private Integer selectMatchedIndex(List<AttendPoint> points,
                                       int index,
                                       List<Integer> candidateIndexes,
                                       List<ActualPunch> punches) {
        if (CollUtil.isEmpty(candidateIndexes)) {
            return null;
        }

        AttendPoint current = points.get(index);
        boolean overlapWithPrev = index > 0 && isOverlapPair(points.get(index - 1), current);
        boolean overlapWithNext = index < points.size() - 1 && isOverlapPair(current, points.get(index + 1));

        // 重叠对里的“下班点”
        if (overlapWithNext && current.getType() == PunchType.OFF_DUTY) {
            AttendPoint next = points.get(index + 1);

            // 1) 优先取共享区间 [当前下班点, 后一个上班点] 内最早一条
            List<Integer> overlapIndexes = candidateIndexes.stream()
                    .filter(idx -> {
                        LocalDateTime time = punches.get(idx).getTime();
                        return !time.isBefore(current.getExpectedTime())
                               && !time.isAfter(next.getExpectedTime());
                    })
                    .collect(Collectors.toList());

            if (CollUtil.isNotEmpty(overlapIndexes)) {
                return overlapIndexes.get(0);
            }

            // 2) 共享区间没有打卡，再回退为普通下班点：取最后一条
            return candidateIndexes.get(candidateIndexes.size() - 1);
        }

        // 重叠对里的“上班点”
        if (overlapWithPrev && current.getType() == PunchType.ON_DUTY) {
            AttendPoint prev = points.get(index - 1);

            List<Integer> overlapIndexes = candidateIndexes.stream()
                    .filter(idx -> {
                        LocalDateTime time = punches.get(idx).getTime();
                        return !time.isBefore(prev.getExpectedTime())
                               && !time.isAfter(current.getExpectedTime());
                    })
                    .collect(Collectors.toList());

            if (CollUtil.isNotEmpty(overlapIndexes)) {
                return overlapIndexes.get(overlapIndexes.size() - 1);
            }

            // 共享区间没有剩余打卡，不要吃掉后面班次的卡
            return null;
        }

        // 普通规则
        return current.getType() == PunchType.ON_DUTY
                ? candidateIndexes.get(0)
                : candidateIndexes.get(candidateIndexes.size() - 1);
    }

    /**
     * 普通规则点状态计算
     */
    private String calcNormalStatus(AttendPoint point, LocalDateTime actualTime) {
        if (point.getType() == PunchType.ON_DUTY) {
            // 业务覆盖班次开始后，顺延出来的上班点
            // 业务结束后的首次回岗打卡按正常处理
            if (point.getMatchStartLimit() != null
                && point.getExpectedTime().equals(point.getMatchStartLimit())) {
                return "正常";
            }
            return actualTime.isAfter(point.getExpectedTime()) ? "迟到" : "正常";
        } else {
            return actualTime.isBefore(point.getExpectedTime()) ? "早退" : "正常";
        }
    }

    /**
     * 找到覆盖“班次开始点”的业务
     * 使用 [start, end) 语义：
     * 开始点被覆盖：biz.start <= point < biz.end
     */
    private BizWindow findBizCoveringStart(LocalDateTime point, List<BizWindow> bizWindows) {
        return bizWindows.stream()
                .filter(biz -> !biz.getStart().isAfter(point) && biz.getEnd().isAfter(point))
                .findFirst()
                .orElse(null);
    }

    /**
     * 找到覆盖“班次结束点”的业务
     * 使用 [start, end] 语义：
     * 结束点被覆盖：biz.start <= point <= biz.end
     */
    private BizWindow findBizCoveringEnd(LocalDateTime point, List<BizWindow> bizWindows) {
        return bizWindows.stream()
                .filter(biz -> !biz.getStart().isAfter(point) && !biz.getEnd().isBefore(point))
                .findFirst()
                .orElse(null);
    }

    /**
     * 构造业务时间窗
     * 优先级：请假 > 出差 > 外出
     */
    private List<BizWindow> buildBizWindows(List<EkpAttendBusinessBO> leaveInfos,
                                            List<EkpAttendBusinessBO> outInfos,
                                            List<EkpAttendBusinessBO> tripInfos) {
        List<BizWindow> list = new ArrayList<>();

        addBizWindows(list, leaveInfos, "请假", 1);
        addBizWindows(list, tripInfos, "出差", 2);
        addBizWindows(list, outInfos, "外出", 3);

        list.sort(Comparator
                .comparingInt(BizWindow::getPriority)
                .thenComparing(BizWindow::getStart));

        return list;
    }

    private void addBizWindows(List<BizWindow> target,
                               List<EkpAttendBusinessBO> bizList,
                               String status,
                               int priority) {
        if (CollUtil.isEmpty(bizList)) {
            return;
        }

        for (EkpAttendBusinessBO item : bizList) {
            if (item == null || item.getStartTime() == null || item.getEndTime() == null) {
                continue;
            }
            target.add(new BizWindow(item.getStartTime(), item.getEndTime(), status, priority));
        }
    }

    private LocalDateTime midpoint(LocalDateTime a, LocalDateTime b) {
        long seconds = Duration.between(a, b).getSeconds();
        return a.plusSeconds(seconds / 2);
    }

    private List<UserAttendRecordVO> sortRawRecords(List<UserAttendRecordVO> actualRecords) {
        return CollUtil.emptyIfNull(actualRecords).stream()
                .sorted(Comparator.comparing(UserAttendRecordVO::getCheckinTime))
                .collect(Collectors.toList());
    }

    /**
     * 构建用户今日请假、外出、出差记录
     */
    public UserLeaveAttendVO buildUserTodayLeaveInfo(List<EkpAttendBusinessBO> leaveInfos,
                                                     List<EkpAttendBusinessBO> outInfos,
                                                     List<EkpAttendBusinessBO> tripInfos) {
        UserLeaveAttendVO vo = new UserLeaveAttendVO();
        vo.setLeaveTimes(formatBizTimes(leaveInfos));
        vo.setOutgoingTimes(formatBizTimes(outInfos));
        vo.setBusinessTripTimes(formatBizTimes(tripInfos));
        return vo;
    }

    /**
     * 格式化业务时间段
     */
    private List<String> formatBizTimes(List<EkpAttendBusinessBO> bizList) {
        if (CollUtil.isEmpty(bizList)) {
            return Collections.emptyList();
        }

        return bizList.stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getStartTime() != null && item.getEndTime() != null)
                .sorted(Comparator.comparing(EkpAttendBusinessBO::getStartTime))
                .map(item -> StrUtil.format("{} - {}",
                        LocalDateTimeUtil.format(item.getStartTime(), "MM-dd HH:mm"),
                        LocalDateTimeUtil.format(item.getEndTime(), "MM-dd HH:mm")))
                .collect(Collectors.toList());
    }

    private enum PunchType {
        ON_DUTY,
        OFF_DUTY
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class AttendPoint {
        /**
         * 用于匹配实际打卡的时间点
         */
        private LocalDateTime expectedTime;

        /**
         * 上班 / 下班
         */
        private PunchType type;

        /**
         * 固定业务状态：请假 / 出差 / 外出
         * 为 null 表示普通规则点，需要按正常/迟到/早退/缺卡判断
         */
        private String fixedStatus;

        /**
         * 匹配打卡的起始限制（含）
         */
        private LocalDateTime matchStartLimit;

        /**
         * 匹配打卡的结束限制（含）
         */
        private LocalDateTime matchEndLimit;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class ActualPunch {
        private LocalDateTime time;
        private String location;
        private Integer exceptionStatus;
        private Integer isReissue;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class BizWindow {
        private LocalDateTime start;
        private LocalDateTime end;
        private String status;
        private Integer priority;
    }
}