
package cn.dong.coade.modules.cmt.support;

import cn.dong.coade.modules.cmt.domain.bo.AttendRuleBO;
import cn.dong.coade.modules.cmt.domain.bo.EkpAttendBusinessBO;
import cn.dong.coade.modules.cmt.domain.enums.AttendRuleType;
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
 * 固定规则：
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
 *
 * IMD 规则：
 * 1. 每个 timeRange 只需要命中一次
 * 2. 若 start == end，则视为“点窗口”，并按点位位置区分匹配范围：
 *    - 第一个点：可用 [当日开始, 下一个窗口开始) 内的打卡，按上班点处理，取最早一条
 *    - 最后一个点：可用 [上一个窗口结束, 当日结束) 内的打卡，按下班点处理，取最后一条
 *    - 中间单点：可用 [上一个窗口结束, 下一个窗口开始) 内的打卡，默认按上班点处理
 *    - 若业务直接覆盖该规则点本身，则该点优先显示业务状态；但若窗口内存在实际打卡，则仍优先按实际打卡认定
 * 3. 若 start != end，则视为“范围窗口”，必须在范围内命中一次
 * 4. 请假会裁剪窗口；若裁剪后窗口为空，则显示“请假”
 * 5. 外出/出差不裁剪窗口；只有完整覆盖剩余窗口时，才显示“外出/出差”
 */
@Component
public class AttendRecordCalculator {

    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * 无需打卡日期展示状态
     */
    private static final String NO_NEED_CHECKIN_STATUS = "无需打卡";

    /**
     * 计算指定日期的考勤结果
     *
     * 兼容旧调用：默认没有“无需打卡日期”配置。
     */
    public List<UserAttendRecordVO> calculate(LocalDate attendDate,
                                              List<UserAttendRecordVO> actualRecords,
                                              AttendRuleBO rule,
                                              List<EkpAttendBusinessBO> leaveInfos,
                                              List<EkpAttendBusinessBO> outInfos,
                                              List<EkpAttendBusinessBO> tripInfos) {
        return calculate(
                attendDate,
                actualRecords,
                rule,
                leaveInfos,
                outInfos,
                tripInfos,
                Collections.emptySet()
        );
    }

    /**
     * 计算指定日期的考勤结果
     *
     * @param noNeedCheckinDates 无需打卡日期配置。命中后，不再生成规则点，也不再判迟到/早退/缺卡。
     */
    public List<UserAttendRecordVO> calculate(LocalDate attendDate,
                                              List<UserAttendRecordVO> actualRecords,
                                              AttendRuleBO rule,
                                              List<EkpAttendBusinessBO> leaveInfos,
                                              List<EkpAttendBusinessBO> outInfos,
                                              List<EkpAttendBusinessBO> tripInfos,
                                              Collection<LocalDate> noNeedCheckinDates) {

        // 无需打卡日期优先级最高：命中后直接返回“无需打卡”，不再按规则计算异常。
        if (isNoNeedCheckinDate(attendDate, noNeedCheckinDates)) {
            return buildNoNeedCheckinResult(attendDate, actualRecords);
        }

        if (rule == null) {
            return sortRawRecords(actualRecords);
        }

        String[][] timeRanges = resolveTimeRanges(rule);
        if (ArrayUtil.isEmpty(timeRanges)) {
            return sortRawRecords(actualRecords);
        }

        int weekDay = attendDate.getDayOfWeek().getValue(); // 1=周一 ... 7=周日
        if (ArrayUtil.isEmpty(rule.getWorkDays()) || !ArrayUtil.contains(rule.getWorkDays(), weekDay)) {
            return sortRawRecords(actualRecords);
        }

        List<BizWindow> bizWindows = buildBizWindows(leaveInfos, outInfos, tripInfos);
        List<ActualPunch> punches = buildActualPunches(actualRecords);

        if (AttendRuleType.IMD.equals(rule.getRuleType())) {
            return calculateImd(attendDate, timeRanges, punches, bizWindows);
        }

        // 固定规则点，只生成“每个班次的上班点/下班点”
        List<AttendPoint> points = buildAttendPoints(attendDate, timeRanges, bizWindows);
        return matchPoints(points, punches, attendDate);
    }


    /**
     * 计算今日考勤
     */
    public List<UserAttendRecordVO> calculateToday(List<UserAttendRecordVO> actualRecords,
                                                   AttendRuleBO rule,
                                                   List<EkpAttendBusinessBO> leaveInfos,
                                                   List<EkpAttendBusinessBO> outInfos,
                                                   List<EkpAttendBusinessBO> tripInfos) {
        return calculate(LocalDate.now(), actualRecords, rule, leaveInfos, outInfos, tripInfos, Collections.emptySet());
    }

    /**
     * 计算今日考勤
     *
     * @param noNeedCheckinDates 无需打卡日期配置。命中今天时，不再生成规则点，也不再判迟到/早退/缺卡。
     */
    public List<UserAttendRecordVO> calculateToday(List<UserAttendRecordVO> actualRecords,
                                                   AttendRuleBO rule,
                                                   List<EkpAttendBusinessBO> leaveInfos,
                                                   List<EkpAttendBusinessBO> outInfos,
                                                   List<EkpAttendBusinessBO> tripInfos,
                                                   Collection<LocalDate> noNeedCheckinDates) {
        return calculate(LocalDate.now(), actualRecords, rule, leaveInfos, outInfos, tripInfos, noNeedCheckinDates);
    }


    /**
     * 判断当前日期是否配置为无需打卡
     */
    private boolean isNoNeedCheckinDate(LocalDate attendDate, Collection<LocalDate> noNeedCheckinDates) {
        return attendDate != null
                && CollUtil.isNotEmpty(noNeedCheckinDates)
                && noNeedCheckinDates.contains(attendDate);
    }

    /**
     * 构造无需打卡日期的返回结果。
     *
     * 说明：
     * 1. 当天有实际打卡记录时，保留实际打卡时间和地点，但统一标记为“无需打卡”，避免进入异常统计。
     * 2. 当天没有实际打卡记录时，返回一条占位记录，方便前端展示“无需打卡”。
     * 3. 如果前端不需要占位行，可以把本方法改成直接 return sortRawRecords(actualRecords)。
     */
    private List<UserAttendRecordVO> buildNoNeedCheckinResult(LocalDate attendDate,
                                                              List<UserAttendRecordVO> actualRecords) {
        List<UserAttendRecordVO> rawRecords = sortRawRecords(actualRecords);
        if (CollUtil.isNotEmpty(rawRecords)) {
            for (UserAttendRecordVO record : rawRecords) {
                record.setRuleCheckinTime(record.getCheckinTime());
                record.setStatus(NO_NEED_CHECKIN_STATUS);
                if (StrUtil.isBlank(record.getLocation())) {
                    record.setLocation("-");
                }
            }
            return rawRecords;
        }

        UserAttendRecordVO vo = new UserAttendRecordVO();
        String time = attendDate.atStartOfDay().format(DATE_TIME_FMT);
        vo.setCheckinTime(time);
        vo.setRuleCheckinTime(time);
        vo.setLocation("-");
        vo.setStatus(NO_NEED_CHECKIN_STATUS);
        return Collections.singletonList(vo);
    }

    /**
     * 解析考勤规则时间段
     * 优先使用 BO 上显式配置的 timeRanges；若为空，则回退到 ruleType 自带规则
     */
    private String[][] resolveTimeRanges(AttendRuleBO rule) {
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
     * 构造实际打卡
     */
    private List<ActualPunch> buildActualPunches(List<UserAttendRecordVO> actualRecords) {
        return CollUtil.emptyIfNull(actualRecords).stream()
                .filter(item -> StrUtil.isNotBlank(item.getCheckinTime()))
                .map(item -> new ActualPunch(
                        LocalDateTime.parse(item.getCheckinTime(), DATE_TIME_FMT),
                        StrUtil.blankToDefault(item.getLocation(), "-"),
                        item.getExceptionStatus(),
                        item.getIsReissue()
                ))
                .sorted(Comparator.comparing(ActualPunch::getTime))
                .collect(Collectors.toList());
    }

    /**
     * IMD 注塑部规则计算
     *
     * 规则补充：
     * 1. 缺卡判定延后到“下一个规则打卡点”再触发，而不是当前窗口一结束就立刻缺卡
     * 2. 但 actual punch 的命中范围仍按 IMD 原规则执行：
     *    - 单点窗口：可在对应基础窗口内命中
     *    - 范围窗口：必须在范围内命中
     */
    private List<UserAttendRecordVO> calculateImd(LocalDate attendDate,
                                                  String[][] timeRanges,
                                                  List<ActualPunch> punches,
                                                  List<BizWindow> bizWindows) {
        List<UserAttendRecordVO> result = new ArrayList<>();
        Set<Integer> usedPunchIndexes = new HashSet<>();
        LocalDateTime now = LocalDateTime.now();
        boolean isToday = LocalDate.now().equals(attendDate);

        List<BizWindow> leaveWindows = filterBizWindows(bizWindows, "请假");
        List<BizWindow> tripWindows = filterBizWindows(bizWindows, "出差");
        List<BizWindow> outWindows = filterBizWindows(bizWindows, "外出");

        for (int i = 0; i < timeRanges.length; i++) {
            String[] range = timeRanges[i];
            LocalDateTime rangeStart = attendDate.atTime(LocalTime.parse(range[0], TIME_FMT));
            LocalDateTime rangeEnd = attendDate.atTime(LocalTime.parse(range[1], TIME_FMT));
            boolean pointWindow = rangeStart.equals(rangeEnd);

            // 每个 IMD timeRange 都只要求命中一次
            List<TimeSlot> baseSlots = buildImdBaseSlots(attendDate, timeRanges, i, rangeStart, rangeEnd);
            List<TimeSlot> effectiveSlots = subtractBizWindows(baseSlots, leaveWindows);

            UserAttendRecordVO vo = new UserAttendRecordVO();
            vo.setRuleCheckinTime(rangeStart.format(DATE_TIME_FMT));

            // 1) 先看当前窗口内是否已经有实际打卡
            // 说明：IMD 允许“提前回岗/提前结束业务后先打卡”，所以这里先按基础窗口找打卡，
            // 不直接用请假裁剪后的窗口做硬限制。若存在实际打卡，则优先按实际打卡认定。
            String pointBizStatus = pointWindow
                    ? resolveImdPointBizStatus(i, timeRanges.length, rangeStart, leaveWindows, tripWindows, outWindows)
                    : null;

            Integer matchedIndex = findImdMatchedIndex(
                    i, timeRanges.length, punches, usedPunchIndexes, baseSlots, pointWindow
            );
            if (matchedIndex != null) {
                ActualPunch matched = punches.get(matchedIndex);
                vo.setCheckinTime(matched.getTime().format(DATE_TIME_FMT));
                vo.setLocation(matched.getLocation());
                vo.setExceptionStatus(matched.getExceptionStatus());
                vo.setIsReissue(matched.getIsReissue());

                if (!pointWindow) {
                    vo.setStatus("正常");
                } else {
                    vo.setStatus(calcImdPointStatus(i, timeRanges.length, rangeStart, matched.getTime(), pointBizStatus));
                }

                usedPunchIndexes.add(matchedIndex);
                result.add(vo);
                continue;
            }

            // 2) 没有实际打卡时，再按业务覆盖结果兜底
            // IMD 单点窗口：如果规则点本身已被业务覆盖，则直接显示业务状态
            if (StrUtil.isNotBlank(pointBizStatus)) {
                fillBizStatusVo(vo, rangeStart, pointBizStatus);
                result.add(vo);
                continue;
            }

            // 3) 请假覆盖了整个窗口：直接显示请假
            if (CollUtil.isEmpty(effectiveSlots)) {
                fillBizStatusVo(vo, rangeStart, "请假");
                result.add(vo);
                continue;
            }

            // 4) 出差 / 外出完整覆盖“请假裁剪后的剩余窗口”：直接显示业务状态
            if (isCoveredByBizWindows(effectiveSlots, tripWindows)) {
                fillBizStatusVo(vo, rangeStart, "出差");
                result.add(vo);
                continue;
            }
            if (isCoveredByBizWindows(effectiveSlots, outWindows)) {
                fillBizStatusVo(vo, rangeStart, "外出");
                result.add(vo);
                continue;
            }

            // 5) 无打卡：
            //    今日且“下一个规则点”未到 -> 待打卡
            //    到了下一个规则点（或非今日） -> 缺卡
            vo.setCheckinTime(rangeStart.format(DATE_TIME_FMT));
            vo.setLocation("-");

            LocalDateTime judgeDeadline = resolveImdJudgeDeadline(attendDate, timeRanges, i);
            if (isToday && now.isBefore(judgeDeadline)) {
                vo.setStatus("待打卡");
            } else {
                vo.setStatus("缺卡");
            }
            result.add(vo);
        }

        return result;
    }

    /**
     * 构造 IMD 单个窗口的基础可匹配区间
     */
    private List<TimeSlot> buildImdBaseSlots(LocalDate attendDate,
                                             String[][] timeRanges,
                                             int index,
                                             LocalDateTime rangeStart,
                                             LocalDateTime rangeEnd) {
        if (rangeStart.equals(rangeEnd)) {
            // 第一个点：按上班点处理，允许早到卡
            if (index == 0) {
                return Collections.singletonList(new TimeSlot(
                        attendDate.atStartOfDay(),
                        resolveImdNextStart(attendDate, timeRanges, index)
                ));
            }

            // 最后一个点：按下班点处理，允许早退卡与正常下班卡
            if (index == timeRanges.length - 1) {
                return Collections.singletonList(new TimeSlot(
                        resolveImdPrevEnd(attendDate, timeRanges, index),
                        attendDate.plusDays(1).atStartOfDay()
                ));
            }

            // 中间单点：放在上一个窗口结束后，到下一个窗口开始前
            return Collections.singletonList(new TimeSlot(
                    resolveImdPrevEnd(attendDate, timeRanges, index),
                    resolveImdNextStart(attendDate, timeRanges, index)
            ));
        }

        // 区间窗口：只允许在区间内命中；end 使用“含义上的包含”，这里转成 [start, end+1秒)
        return Collections.singletonList(new TimeSlot(rangeStart, toEndExclusive(rangeEnd)));
    }

    /**
     * IMD 当前窗口的下一个开始边界（不含）
     */
    private LocalDateTime resolveImdNextStart(LocalDate attendDate,
                                              String[][] timeRanges,
                                              int index) {
        if (index >= timeRanges.length - 1) {
            return attendDate.plusDays(1).atStartOfDay();
        }
        String[] next = timeRanges[index + 1];
        return attendDate.atTime(LocalTime.parse(next[0], TIME_FMT));
    }

    /**
     * IMD 当前窗口的上一个结束边界（含义上用于起点）
     */
    private LocalDateTime resolveImdPrevEnd(LocalDate attendDate,
                                            String[][] timeRanges,
                                            int index) {
        if (index <= 0) {
            return attendDate.atStartOfDay();
        }
        String[] prev = timeRanges[index - 1];
        return attendDate.atTime(LocalTime.parse(prev[1], TIME_FMT));
    }

    private LocalDateTime toEndExclusive(LocalDateTime endInclusive) {
        return endInclusive.plusSeconds(1);
    }

    private List<BizWindow> filterBizWindows(List<BizWindow> bizWindows, String status) {
        return bizWindows.stream()
                .filter(item -> StrUtil.equals(item.getStatus(), status))
                .collect(Collectors.toList());
    }

    /**
     * 使用请假时间窗裁剪 IMD 可匹配窗口
     */
    private List<TimeSlot> subtractBizWindows(List<TimeSlot> baseSlots, List<BizWindow> bizWindows) {
        List<TimeSlot> result = new ArrayList<>(baseSlots);
        for (BizWindow bizWindow : CollUtil.emptyIfNull(bizWindows)) {
            TimeSlot remove = new TimeSlot(bizWindow.getStart(), toEndExclusive(bizWindow.getEnd()));
            result = subtractTimeSlotList(result, remove);
            if (CollUtil.isEmpty(result)) {
                return Collections.emptyList();
            }
        }
        return result;
    }

    private List<TimeSlot> subtractTimeSlotList(List<TimeSlot> slots, TimeSlot remove) {
        List<TimeSlot> result = new ArrayList<>();
        for (TimeSlot slot : slots) {
            result.addAll(subtractTimeSlot(slot, remove));
        }
        return result;
    }

    /**
     * 从 slot 中移除 remove，返回剩余片段
     * 都按 [start, endExclusive) 处理
     */
    private List<TimeSlot> subtractTimeSlot(TimeSlot slot, TimeSlot remove) {
        // 无交集
        if (!slot.getStart().isBefore(remove.getEndExclusive()) || !remove.getStart().isBefore(slot.getEndExclusive())) {
            return Collections.singletonList(slot);
        }

        List<TimeSlot> result = new ArrayList<>();

        // 左侧剩余
        if (slot.getStart().isBefore(remove.getStart())) {
            TimeSlot left = new TimeSlot(slot.getStart(), min(slot.getEndExclusive(), remove.getStart()));
            if (left.isValid()) {
                result.add(left);
            }
        }

        // 右侧剩余
        if (remove.getEndExclusive().isBefore(slot.getEndExclusive())) {
            TimeSlot right = new TimeSlot(max(slot.getStart(), remove.getEndExclusive()), slot.getEndExclusive());
            if (right.isValid()) {
                result.add(right);
            }
        }

        return result;
    }

    /**
     * 判断业务时间窗是否完整覆盖所有可匹配片段
     */
    private boolean isCoveredByBizWindows(List<TimeSlot> slots, List<BizWindow> bizWindows) {
        if (CollUtil.isEmpty(slots)) {
            return true;
        }
        List<TimeSlot> remaining = new ArrayList<>(slots);
        for (BizWindow bizWindow : CollUtil.emptyIfNull(bizWindows)) {
            remaining = subtractTimeSlotList(remaining, new TimeSlot(bizWindow.getStart(), toEndExclusive(bizWindow.getEnd())));
            if (CollUtil.isEmpty(remaining)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 解析 IMD 单点窗口在当前时刻应该展示的业务状态。
     *
     * 说明：
     * 1. 第一个单点窗口通常表示“上班点”，覆盖语义与固定规则上班点保持一致： [start, end)
     *    例如请假到 08:00 结束，不应视为覆盖 08:00。
     * 2. 最后一个单点窗口通常表示“下班点”，覆盖语义与固定规则下班点保持一致： [start, end]
     *    例如请假到 20:30 结束，应视为覆盖 20:30。
     * 3. 中间的单点窗口如果存在，采用包含端点的宽松语义 [start, end]。
     * 4. 优先级：请假 > 出差 > 外出。
     */
    private String resolveImdPointBizStatus(int index,
                                            int total,
                                            LocalDateTime point,
                                            List<BizWindow> leaveWindows,
                                            List<BizWindow> tripWindows,
                                            List<BizWindow> outWindows) {
        if (isImdPointCovered(point, leaveWindows, index, total)) {
            return "请假";
        }
        if (isImdPointCovered(point, tripWindows, index, total)) {
            return "出差";
        }
        if (isImdPointCovered(point, outWindows, index, total)) {
            return "外出";
        }
        return null;
    }

    private boolean isImdPointCovered(LocalDateTime point,
                                      List<BizWindow> bizWindows,
                                      int index,
                                      int total) {
        return CollUtil.emptyIfNull(bizWindows).stream().anyMatch(biz -> {
            if (index == 0) {
                // 第一个点：按上班点处理，结束时刻不算覆盖
                return !biz.getStart().isAfter(point) && biz.getEnd().isAfter(point);
            }
            if (index == total - 1) {
                // 最后一个点：按下班点处理，结束时刻算覆盖
                return !biz.getStart().isAfter(point) && !biz.getEnd().isBefore(point);
            }
            // 中间单点：宽松按包含端点处理
            return !biz.getStart().isAfter(point) && !biz.getEnd().isBefore(point);
        });
    }

    private List<Integer> findPunchIndexesInSlots(List<ActualPunch> punches,
                                                  Set<Integer> usedPunchIndexes,
                                                  List<TimeSlot> slots) {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < punches.size(); i++) {
            if (usedPunchIndexes.contains(i)) {
                continue;
            }
            ActualPunch punch = punches.get(i);
            if (containsTime(slots, punch.getTime())) {
                result.add(i);
            }
        }
        return result;
    }

    private Integer findImdMatchedIndex(int index,
                                        int total,
                                        List<ActualPunch> punches,
                                        Set<Integer> usedPunchIndexes,
                                        List<TimeSlot> slots,
                                        boolean pointWindow) {
        List<Integer> candidates = findPunchIndexesInSlots(punches, usedPunchIndexes, slots);
        if (CollUtil.isEmpty(candidates)) {
            return null;
        }

        if (!pointWindow) {
            return candidates.get(0);
        }

        // 最后一个单点按下班点处理：取最后一条
        if (index == total - 1) {
            return candidates.get(candidates.size() - 1);
        }

        // 其他单点按上班点处理：取第一条
        return candidates.get(0);
    }

    private String calcImdPointStatus(int index,
                                      int total,
                                      LocalDateTime ruleTime,
                                      LocalDateTime actualTime,
                                      String pointBizStatus) {
        // 规则点已被业务覆盖，但窗口内存在实际打卡时，按实际打卡正常认定
        if (StrUtil.isNotBlank(pointBizStatus)) {
            return "正常";
        }

        // 最后一个单点按下班点处理
        if (index == total - 1) {
            return actualTime.isBefore(ruleTime) ? "早退" : "正常";
        }

        // 其他单点按上班点处理
        return actualTime.isAfter(ruleTime) ? "迟到" : "正常";
    }

    private boolean containsTime(List<TimeSlot> slots, LocalDateTime time) {
        for (TimeSlot slot : slots) {
            if (slot.contains(time)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasFutureSlot(List<TimeSlot> slots, LocalDateTime now) {
        for (TimeSlot slot : slots) {
            if (slot.getEndExclusive().isAfter(now)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 请假顺延出来的上班点：
     * - expectedTime == matchStartLimit 表示规则点被顺延到了业务结束时间
     * - 但员工可能会提前回岗并先打卡，因此匹配阶段不能把业务结束时间作为硬限制
     */
    private boolean isLeaveAdjustedOnDutyPoint(AttendPoint point) {
        return point != null
                && point.getType() == PunchType.ON_DUTY
                && StrUtil.isBlank(point.getFixedStatus())
                && point.getMatchStartLimit() != null
                && point.getExpectedTime().equals(point.getMatchStartLimit());
    }

    private void fillBizStatusVo(UserAttendRecordVO vo, LocalDateTime ruleTime, String status) {
        vo.setCheckinTime(ruleTime.format(DATE_TIME_FMT));
        vo.setRuleCheckinTime(ruleTime.format(DATE_TIME_FMT));
        vo.setLocation("-");
        vo.setStatus(status);
    }

    /**
     * 构造固定规则点
     */
    private List<AttendPoint> buildAttendPoints(LocalDate attendDate,
                                                String[][] timeRanges,
                                                List<BizWindow> bizWindows) {
        List<AttendPoint> points = new ArrayList<>();

        for (String[] range : timeRanges) {
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

        // 外出 / 出差：只显示状态，不重建打卡点
        if (!Boolean.TRUE.equals(bizAtStart.getRebuildPoint())) {
            return new AttendPoint(sessionStart, PunchType.ON_DUTY, bizAtStart.getStatus(), null, null);
        }

        // ===== 以下才是请假的重建逻辑 =====

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

        // 外出 / 出差：只显示状态，不重建打卡点
        if (!Boolean.TRUE.equals(bizAtEnd.getRebuildPoint())) {
            return new AttendPoint(sessionEnd, PunchType.OFF_DUTY, bizAtEnd.getStatus(), null, null);
        }

        // ===== 以下才是请假的重建逻辑 =====

        // 业务从班次开始就覆盖到结束：整个班次都处于业务状态
        if (!bizAtEnd.getStart().isAfter(sessionStart)) {
            return new AttendPoint(sessionEnd, PunchType.OFF_DUTY, bizAtEnd.getStatus(), null, null);
        }

        // 业务在班中开始，并覆盖到下班点：
        // 下班点前移到业务开始时间
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
     *
     * 新规则：
     * 1. 当前规则点的候选打卡范围一直延续到“下一个规则点”
     * 2. 当前规则点是否判缺卡，也延后到“下一个规则点”再触发
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

                // 请假顺延出来的上班点，允许员工在业务结束前提前回岗打卡
                // 因此这里不再把 matchStartLimit 作为硬性的最早打卡限制。
                if (current.getMatchStartLimit() != null
                        && punch.getTime().isBefore(current.getMatchStartLimit())
                        && !isLeaveAdjustedOnDutyPoint(current)) {
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
            // 这类规则点只用于展示业务状态，不再绑定真实打卡记录。
            //
            // 原因：
            // 例如规则为 08:00-11:30、12:30-17:30，请假 08:00-12:30，
            // 员工 12:23 提前回岗打卡。
            //
            // 旧逻辑会让 11:30 的“请假”点吃掉 12:23，导致出现：
            // 12:23 请假
            // 12:23 正常
            //
            // 正确逻辑应该是：
            // 08:00 请假
            // 11:30 请假
            // 12:23 正常
            //
            // 所以固定业务状态点直接按规则点时间展示，不占用真实打卡。
            if (StrUtil.isNotBlank(current.getFixedStatus())) {
                vo.setCheckinTime(current.getExpectedTime().format(DATE_TIME_FMT));
                vo.setRuleCheckinTime(current.getExpectedTime().format(DATE_TIME_FMT));
                vo.setLocation("-");
                vo.setStatus(current.getFixedStatus());
                result.add(vo);
                continue;
            }

            LocalDateTime judgeDeadline = resolveJudgeDeadline(points, i, attendDate);

            // 今日且“下一个规则点”未到，只有在还没匹配到实际打卡时才显示待打卡
            if (isToday && matched == null && now.isBefore(judgeDeadline)) {
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
     *
     * 现在统一使用“上一个规则点时间”作为开始边界，
     * 这样当前规则点可以持续接收直到下一个规则点之前的打卡。
     */
    private LocalDateTime resolveStartBoundary(List<AttendPoint> points, int index, LocalDate attendDate) {
        if (index == 0) {
            return attendDate.atStartOfDay();
        }
        return points.get(index - 1).getExpectedTime();
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
     *
     * 现在统一使用“下一个规则点时间”作为结束边界（右开区间），
     * 不再用中点截断。
     */
    private LocalDateTime resolveEndBoundary(List<AttendPoint> points, int index, LocalDate attendDate) {
        if (index == points.size() - 1) {
            return attendDate.plusDays(1).atStartOfDay();
        }
        return points.get(index + 1).getExpectedTime();
    }

    /**
     * 当前规则点何时开始判缺卡：
     * - 有下一个规则点：到下一个规则点再判
     * - 没有下一个规则点：到次日 00:00 再判
     */
    private LocalDateTime resolveJudgeDeadline(List<AttendPoint> points, int index, LocalDate attendDate) {
        if (index < points.size() - 1) {
            return points.get(index + 1).getExpectedTime();
        }
        return attendDate.plusDays(1).atStartOfDay();
    }

    /**
     * IMD 缺卡判定时间：
     * - 有下一个 timeRange：到下一个 timeRange 的开始时间再判
     * - 没有下一个 timeRange：到次日 00:00 再判
     */
    private LocalDateTime resolveImdJudgeDeadline(LocalDate attendDate,
                                                  String[][] timeRanges,
                                                  int index) {
        if (index < timeRanges.length - 1) {
            return attendDate.atTime(LocalTime.parse(timeRanges[index + 1][0], TIME_FMT));
        }
        return attendDate.plusDays(1).atStartOfDay();
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

        // 请假顺延出来的上班点：
        // 允许员工在 expectedTime 之前提前回岗打卡。
        // 这类点优先取“expectedTime 之前最近的一次打卡”；
        // 如果没有，再退回取候选集中的第一条。
        if (isLeaveAdjustedOnDutyPoint(current)) {
            Integer latestBeforeExpected = null;
            for (Integer idx : candidateIndexes) {
                LocalDateTime time = punches.get(idx).getTime();
                if (!time.isAfter(current.getExpectedTime())) {
                    latestBeforeExpected = idx;
                } else {
                    break;
                }
            }
            if (latestBeforeExpected != null) {
                return latestBeforeExpected;
            }
            return candidateIndexes.get(0);
        }

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

            // 共享区间没有打卡时，仍要优先满足“当前班次的上班点”。
            // 例如 11:30 下班、12:30 上班、17:30 下班，若只有 15:00 一次打卡，
            // 这条卡应先认定为 12:30 上班迟到，而不是被 17:30 下班点吃掉判成早退。
            // 因此这里回退取当前候选集中的第一条，让“同一班次先上班、后下班”。
            return candidateIndexes.get(0);
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

        // 只有请假允许重建打卡点
        addBizWindows(list, leaveInfos, "请假", 1, true);

        // 出差、外出只显示状态，不重建打卡点
        addBizWindows(list, tripInfos, "出差", 2, false);
        addBizWindows(list, outInfos, "外出", 3, false);

        list.sort(Comparator
                .comparingInt(BizWindow::getPriority)
                .thenComparing(BizWindow::getStart));

        return list;
    }

    private void addBizWindows(List<BizWindow> target,
                               List<EkpAttendBusinessBO> bizList,
                               String status,
                               int priority,
                               boolean rebuildPoint) {
        if (CollUtil.isEmpty(bizList)) {
            return;
        }

        for (EkpAttendBusinessBO item : bizList) {
            if (item == null || item.getStartTime() == null || item.getEndTime() == null) {
                continue;
            }
            target.add(new BizWindow(
                    item.getStartTime(),
                    item.getEndTime(),
                    status,
                    priority,
                    rebuildPoint
            ));
        }
    }

    private LocalDateTime midpoint(LocalDateTime a, LocalDateTime b) {
        long seconds = Duration.between(a, b).getSeconds();
        return a.plusSeconds(seconds / 2);
    }

    private LocalDateTime min(LocalDateTime a, LocalDateTime b) {
        return a.isBefore(b) ? a : b;
    }

    private LocalDateTime max(LocalDateTime a, LocalDateTime b) {
        return a.isAfter(b) ? a : b;
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

        /**
         * 是否允许重建打卡点
         * true：请假
         * false：外出 / 出差
         */
        private Boolean rebuildPoint;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class TimeSlot {
        /**
         * 起始（含）
         */
        private LocalDateTime start;

        /**
         * 结束（不含）
         */
        private LocalDateTime endExclusive;

        private boolean contains(LocalDateTime time) {
            return !time.isBefore(start) && time.isBefore(endExclusive);
        }

        private boolean isValid() {
            return start != null && endExclusive != null && start.isBefore(endExclusive);
        }
    }
}
