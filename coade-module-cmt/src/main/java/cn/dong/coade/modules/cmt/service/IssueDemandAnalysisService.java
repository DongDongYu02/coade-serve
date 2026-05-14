package cn.dong.coade.modules.cmt.service;

import cn.dong.coade.modules.cmt.constants.CmtLocalConstants;
import cn.dong.coade.modules.cmt.domain.entity.CmtIssueDemand;
import cn.dong.coade.modules.cmt.domain.query.IssueDemandAnalysisQuery;
import cn.dong.coade.modules.cmt.domain.vo.*;
import cn.dong.coade.modules.cmt.mapper.CmtIssueDemandMapper;
import cn.dong.nexus.common.api.DataDictCommonApi;
import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.common.domain.bo.DataDictBO;
import cn.dong.nexus.common.utils.CommonUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class IssueDemandAnalysisService {
    private static final String[] WEEK_LABELS = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
    private final ICmtIssueDemandService issueDemandService;
    private final DataDictCommonApi dataDictCommonApi;

    /**
     * 获取总览统计
     */
    public IssueDemandOverviewVO getOverview(@ParameterObject IssueDemandAnalysisQuery query) {
        LocalDateTime[] timeRange = query.buildTimeRange();
        List<CmtIssueDemand> records = issueDemandService.lambdaQuery().ge(CmtIssueDemand::getCreateTime, timeRange[0]).lt(CmtIssueDemand::getCreateTime, timeRange[1]).list();
        IssueDemandOverviewVO overviewVO = new IssueDemandOverviewVO();
        if (records.isEmpty()) {
            return overviewVO;
        }
        // 总数
        long totalCount = records.size();
        // 优化反馈总数
        long issueFeedbackCount = records.stream().filter(item -> CmtLocalConstants.ISSUE_DEMAND_TYPE.ISSUE.equals(item.getType())).count();
        // 需求开发总数
        long demandDevelopmentCount = records.stream().filter(item -> CmtLocalConstants.ISSUE_DEMAND_TYPE.DEMAND.equals(item.getType())).count();
        // 紧急总数
        long urgentCount = records.stream().filter(item -> GlobalConstants.INT_YES.equals(item.getIsUrgent())).count();
        // 待处理总数
        long pendingHandleCount = records.stream().filter(item -> CmtLocalConstants.ISSUE_DEMAND_STATUS.PENDING.equals(item.getStatus())).count();
        // 开发中数
        long devCount = records.stream().filter(item -> CmtLocalConstants.ISSUE_DEMAND_STATUS.IN_PROGRESS.equals(item.getStatus())).count();
        // 评估中数量
        long assessingCount = records.stream().filter(item -> CmtLocalConstants.ISSUE_DEMAND_STATUS.ASSESSING.equals(item.getStatus())).count();
        // 处理中总数
        long processingCount = records.stream().filter(item -> CmtLocalConstants.ISSUE_DEMAND_STATUS.PROCESSING.contains(item.getStatus())).count();
        // 待验收
        long pendingAcceptanceCount = records.stream().filter(item -> CmtLocalConstants.ISSUE_DEMAND_STATUS.PENDING_ACCEPT.equals(item.getStatus())).count();
        // 已完成数量
        long completedCount = records.stream().filter(item -> CmtLocalConstants.ISSUE_DEMAND_STATUS.COMPLETED.equals(item.getStatus())).count();
        // 作废数量
        long voidedCount = records.stream().filter(item -> CmtLocalConstants.ISSUE_DEMAND_STATUS.VOIDED.equals(item.getStatus())).count();
        // 有效率
        BigDecimal effectiveRate = BigDecimal.valueOf(totalCount - voidedCount).divide(BigDecimal.valueOf(totalCount), 4, RoundingMode.HALF_UP);
        // 完成率
        BigDecimal completionRate = BigDecimal.valueOf(completedCount).divide(BigDecimal.valueOf(totalCount), 4, RoundingMode.HALF_UP);
        overviewVO.setTotalCount(totalCount);
        overviewVO.setIssueFeedbackCount(issueFeedbackCount);
        overviewVO.setDemandDevelopmentCount(demandDevelopmentCount);
        overviewVO.setUrgentCount(urgentCount);
        overviewVO.setPendingHandleCount(pendingHandleCount);
        overviewVO.setProcessingCount(processingCount);
        overviewVO.setPendingAcceptanceCount(pendingAcceptanceCount);
        overviewVO.setCompletedCount(completedCount);
        overviewVO.setCompletionRate(completionRate);
        overviewVO.setCompletionRateText(completionRate.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP) + "%");
        overviewVO.setDevCount(devCount);
        overviewVO.setAssessingCount(assessingCount);
        overviewVO.setVoidedCount(voidedCount);
        overviewVO.setEffectiveRate(effectiveRate);
        overviewVO.setEffectiveRateText(effectiveRate.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP) + "%");
        return overviewVO;
    }

    /**
     * 上报趋势
     */
    public List<IssueDemandSubmitTrendVO> getSubmitTrend(IssueDemandAnalysisQuery query) {
        List<IssueDemandSubmitTrendVO> data = initSubmitTrendData(query.getTimeRange());
        LocalDateTime[] timeRange = query.buildTimeRange();
        List<CmtIssueDemand> records = issueDemandService.lambdaQuery().ge(CmtIssueDemand::getCreateTime, timeRange[0]).le(CmtIssueDemand::getCreateTime, timeRange[1]).list();
        if (records.isEmpty()) {
            return data;
        }
        // 根据时间单位分组
        Map<String, List<CmtIssueDemand>> collect = records.stream().collect(Collectors.groupingBy(item -> {
            LocalDateTime createTime = item.getCreateTime();
            return switch (query.getTimeRange()) {
                case CmtLocalConstants.ANALYSIS_TIME_RANGE.THIS_YEAR,
                     CmtLocalConstants.ANALYSIS_TIME_RANGE.THIS_QUARTER,
                     CmtLocalConstants.ANALYSIS_TIME_RANGE.LAST_QUARTER -> createTime.getMonthValue() + "月";
                case CmtLocalConstants.ANALYSIS_TIME_RANGE.THIS_MONTH,
                     CmtLocalConstants.ANALYSIS_TIME_RANGE.LAST_MONTH -> createTime.getDayOfMonth() + "日";
                case CmtLocalConstants.ANALYSIS_TIME_RANGE.THIS_WEEK, CmtLocalConstants.ANALYSIS_TIME_RANGE.LAST_WEEK ->
                        WEEK_LABELS[createTime.getDayOfWeek().getValue() - 1];
                default -> "";
            };
        }));
        data.forEach(item -> {
            List<CmtIssueDemand> dateItems = collect.get(item.getDate());
            if (CollUtil.isNotEmpty(dateItems)) {
                long issuesCount = dateItems.stream().filter(di -> Objects.equals(di.getType(), CmtLocalConstants.ISSUE_DEMAND_TYPE.ISSUE)).count();
                long demandCount = dateItems.stream().filter(di -> Objects.equals(di.getType(), CmtLocalConstants.ISSUE_DEMAND_TYPE.DEMAND)).count();
                item.setIssueCount(issuesCount);
                item.setDemandCount(demandCount);
            }
        });
        return data;
    }

    /**
     * 获取处理节点概览
     */
    public IssueDemandProcessOverviewVO getProcessOverview(IssueDemandAnalysisQuery query) {
        LocalDateTime[] timeRange = query.buildTimeRange();
        List<CmtIssueDemand> records = issueDemandService.lambdaQuery().ge(CmtIssueDemand::getCreateTime, timeRange[0]).lt(CmtIssueDemand::getCreateTime, timeRange[1]).list();
        IssueDemandProcessOverviewVO overviewVO = new IssueDemandProcessOverviewVO();
        if (records.isEmpty()) {
            return overviewVO;
        }
        LocalDateTime now = LocalDateTime.now();
        // 待处理总数
        long pendingHandleCount = records.stream().filter(item -> CmtLocalConstants.ISSUE_DEMAND_STATUS.PENDING.equals(item.getStatus())).count();
        // 待处理超过72小时数量
        long pendingHandleOver72hCount = records.stream().filter(item -> CmtLocalConstants.ISSUE_DEMAND_STATUS.PENDING.equals(item.getStatus()) && item.getCreateTime().isBefore(now.minusHours(72))).count();
        // 开发中数
        long devCount = records.stream().filter(item -> CmtLocalConstants.ISSUE_DEMAND_STATUS.IN_PROGRESS.equals(item.getStatus())).count();
        // 计划内数量
        long developingInPlanCount = records.stream()
                .filter(item -> CmtLocalConstants.ISSUE_DEMAND_STATUS.IN_PROGRESS.equals(item.getStatus())
                        && item.getPlanFinishTime().isAfter(now))
                .count();
        // 评估中数量
        long evaluatingCount = records.stream().filter(item -> CmtLocalConstants.ISSUE_DEMAND_STATUS.ASSESSING.equals(item.getStatus())).count();
        // 待验收
        long pendingAcceptanceCount = records.stream().filter(item -> CmtLocalConstants.ISSUE_DEMAND_STATUS.PENDING_ACCEPT.equals(item.getStatus())).count();
        // 验收逾期数量
        long acceptanceOverdueCount = this.computedAcceptanceOverdueCount(records);
        // 平均响应时长（天）
        BigDecimal avgResponseDays = this.computedAvgResponseDays(records);
        overviewVO.setPendingHandleCount(pendingHandleCount);
        overviewVO.setPendingHandleOver72hCount(pendingHandleOver72hCount);
        overviewVO.setDevelopingCount(devCount);
        overviewVO.setDevelopingInPlanCount(developingInPlanCount);
        overviewVO.setEvaluatingCount(evaluatingCount);
        overviewVO.setAvgResponseDays(avgResponseDays);
        overviewVO.setAvgResponseDaysFormat(avgResponseDays + "天");
        overviewVO.setAcceptanceOverdueCount(acceptanceOverdueCount);
        overviewVO.setPendingAcceptanceCount(pendingAcceptanceCount);
        return overviewVO;
    }

    private Long computedAcceptanceOverdueCount(List<CmtIssueDemand> records) {
        if (CollUtil.isEmpty(records)) {
            return 0L;
        }

        return records.stream()
                .filter(Objects::nonNull)
                .filter(item -> Objects.nonNull(item.getActualFinishTime()))
                .filter(item -> Objects.equals(this.computedAcceptanceIsOverdue(item), 1))
                .count();
    }

    private Integer computedAcceptanceIsOverdue(CmtIssueDemand item) {
        if (Objects.isNull(item) || Objects.isNull(item.getActualFinishTime())) {
            return null;
        }

        Integer status = item.getStatus();

        // 实际完成时间，也就是进入待验收的时间
        LocalDateTime actualFinishTime = item.getActualFinishTime();

        // 验收截止时间：完成开发后 24 小时内验收
        LocalDateTime acceptanceDeadlineTime = actualFinishTime.plusHours(24);

        // 已完成：用验收通过时间判断
//        if (CmtLocalConstants.ISSUE_DEMAND_STATUS.COMPLETED.equals(status)) {
//            LocalDateTime acceptanceTime = item.getAcceptanceTime();
//            if (Objects.isNull(acceptanceTime)) {
//                return null;
//            }
//
//            return acceptanceTime.isAfter(acceptanceDeadlineTime) ? 1 : 0;
//        }

        // 待验收：用当前时间判断
        if (CmtLocalConstants.ISSUE_DEMAND_STATUS.PENDING_ACCEPT.equals(status)) {
            return LocalDateTime.now().isAfter(acceptanceDeadlineTime) ? 1 : 0;
        }

        // 其他状态不参与验收逾期判断
        return null;
    }


    private BigDecimal computedAvgResponseDays(List<CmtIssueDemand> records) {
        if (CollUtil.isEmpty(records)) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalMinutes = BigDecimal.ZERO;
        int validCount = 0;

        for (CmtIssueDemand issueDemand : records) {
            LocalDateTime createTime = issueDemand.getCreateTime();
            /*
             * 响应结束时间：
             * 1. 有开发开始时间，说明已经进入处理/开发，使用 devStartTime
             * 2. 没有开发开始时间，但有作废时间，说明未开发前被作废，使用 voidedTime
             * 3. 两个时间都没有，不参与统计
             */
            LocalDateTime responseEndTime;
            if (Objects.nonNull(issueDemand.getDevStartTime())) {
                responseEndTime = issueDemand.getDevStartTime();
            } else if (Objects.nonNull(issueDemand.getVoidedTime())) {
                responseEndTime = issueDemand.getVoidedTime();
            } else {
                continue;
            }


            long minutes = ChronoUnit.MINUTES.between(createTime, responseEndTime);
            totalMinutes = totalMinutes.add(BigDecimal.valueOf(minutes));
            validCount++;
        }

        if (validCount == 0) {
            return BigDecimal.ZERO;
        }

        // 平均响应天数 = 总分钟数 / 有效数量 / 1440
        BigDecimal avgResponseDays = totalMinutes
                .divide(BigDecimal.valueOf(validCount), 4, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(24 * 60), 2, RoundingMode.HALF_UP);

        // 去掉尾数 0
        return avgResponseDays.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : avgResponseDays.stripTrailingZeros();
    }

    private Duration computedAvgResponseDuration(List<CmtIssueDemand> records) {
        if (CollUtil.isEmpty(records)) {
            return Duration.ZERO;
        }

        Duration totalDuration = Duration.ZERO;
        long validCount = 0L;

        for (CmtIssueDemand issueDemand : records) {
            if (Objects.isNull(issueDemand) || Objects.isNull(issueDemand.getCreateTime())) {
                continue;
            }

            LocalDateTime createTime = issueDemand.getCreateTime();

            /*
             * 响应结束时间：
             * 1. 有开发开始时间，说明已经进入处理/开发，使用 devStartTime
             * 2. 没有开发开始时间，但有作废时间，说明未开发前被作废，使用 voidedTime
             * 3. 两个时间都没有，不参与统计
             */
            LocalDateTime responseEndTime;
            if (Objects.nonNull(issueDemand.getDevStartTime())) {
                responseEndTime = issueDemand.getDevStartTime();
            } else if (Objects.nonNull(issueDemand.getVoidedTime())) {
                responseEndTime = issueDemand.getVoidedTime();
            } else {
                continue;
            }

            // 防止异常数据：结束时间早于创建时间
            if (responseEndTime.isBefore(createTime)) {
                continue;
            }

            Duration duration = Duration.between(createTime, responseEndTime);
            totalDuration = totalDuration.plus(duration);
            validCount++;
        }

        if (validCount == 0) {
            return Duration.ZERO;
        }

        return totalDuration.dividedBy(validCount);
    }

    private List<IssueDemandSubmitTrendVO> initSubmitTrendData(Integer timeRange) {
        if (Objects.isNull(timeRange)) {
            return List.of();
        }

        LocalDate now = LocalDate.now();

        return switch (timeRange) {
            // 本年趋势：1月 - 12月
            case CmtLocalConstants.ANALYSIS_TIME_RANGE.THIS_YEAR -> buildMonthTrendData(1, 12);

            // 本季度趋势：当前季度的三个月
            case CmtLocalConstants.ANALYSIS_TIME_RANGE.THIS_QUARTER -> {
                int currentQuarter = getQuarter(now);
                int startMonth = (currentQuarter - 1) * 3 + 1;
                yield buildMonthTrendData(startMonth, startMonth + 2);
            }

            // 本月趋势：1日 - 本月最后一天
            case CmtLocalConstants.ANALYSIS_TIME_RANGE.THIS_MONTH -> buildDayTrendData(now);

            // 本周趋势：周一 - 周日
            case CmtLocalConstants.ANALYSIS_TIME_RANGE.THIS_WEEK -> buildWeekTrendData(now);

            // 上季度趋势：上季度的三个月
            case CmtLocalConstants.ANALYSIS_TIME_RANGE.LAST_QUARTER -> {
                LocalDate lastQuarterDate = now.minusMonths(3);
                int lastQuarter = getQuarter(lastQuarterDate);
                int startMonth = (lastQuarter - 1) * 3 + 1;
                yield buildMonthTrendData(startMonth, startMonth + 2);
            }

            // 上月趋势：1日 - 上月最后一天
            case CmtLocalConstants.ANALYSIS_TIME_RANGE.LAST_MONTH -> buildDayTrendData(now.minusMonths(1));

            // 上周趋势：周一 - 周日
            case CmtLocalConstants.ANALYSIS_TIME_RANGE.LAST_WEEK -> buildWeekTrendData(now.minusWeeks(1));

            default -> List.of();
        };
    }

    /**
     * 构建月份趋势
     */
    private List<IssueDemandSubmitTrendVO> buildMonthTrendData(int startMonth, int endMonth) {
        return IntStream.rangeClosed(startMonth, endMonth).mapToObj(month -> buildSubmitTrendVO(month + "月")).toList();
    }

    /**
     * 构建某个月的每日趋势
     */
    private List<IssueDemandSubmitTrendVO> buildDayTrendData(LocalDate date) {
        YearMonth yearMonth = YearMonth.from(date);

        return IntStream.rangeClosed(1, yearMonth.lengthOfMonth()).mapToObj(day -> buildSubmitTrendVO(day + "日")).toList();
    }

    /**
     * 构建周趋势，默认周一到周日
     */
    private List<IssueDemandSubmitTrendVO> buildWeekTrendData(LocalDate date) {
        LocalDate monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        return IntStream.range(0, 7).mapToObj(index -> {
            LocalDate currentDate = monday.plusDays(index);
            int weekIndex = currentDate.getDayOfWeek().getValue() - 1;
            return buildSubmitTrendVO(WEEK_LABELS[weekIndex]);
        }).toList();
    }

    /**
     * 获取季度
     */
    private int getQuarter(LocalDate date) {
        return (date.getMonthValue() - 1) / 3 + 1;
    }

    /**
     * 构建趋势 VO
     */
    private IssueDemandSubmitTrendVO buildSubmitTrendVO(String date) {
        IssueDemandSubmitTrendVO trendVO = new IssueDemandSubmitTrendVO();
        trendVO.setIssueCount(0L);
        trendVO.setDemandCount(0L);
        trendVO.setDate(date);

        return trendVO;
    }


    /**
     * 获取系统分布统计
     */
    public List<IssueDemandSystemDistributionVO> getSystemDistribution(IssueDemandAnalysisQuery query) {
        LocalDateTime[] timeRange = query.buildTimeRange();
        List<CmtIssueDemand> issueDemands = issueDemandService.lambdaQuery()
                .ge(CmtIssueDemand::getCreateTime, timeRange[0])
                .le(CmtIssueDemand::getCreateTime, timeRange[1])
                .list();
        if (issueDemands.isEmpty()) {
            return List.of();
        }
        List<DataDictBO> dataDictItems = dataDictCommonApi._getItemsByCode(GlobalConstants.DATA_DICT_CODE.GSXT);
        // 根据系统类型分组统计
        Map<Integer, Long> systemTypeGroupCount = issueDemands.stream().collect(Collectors.groupingBy(CmtIssueDemand::getSystemType, Collectors.counting()));
        List<IssueDemandSystemDistributionVO> result = dataDictItems.stream().map(item -> {
            IssueDemandSystemDistributionVO vo = new IssueDemandSystemDistributionVO();
            Integer systemType = Integer.parseInt(item.getValue());
            Long count = systemTypeGroupCount.getOrDefault(systemType, 0L);
            vo.setSystemType(systemType);
            vo.setSystemTypeText(item.getText());
            vo.setCount(count);
            return vo;
        }).collect(Collectors.toCollection(ArrayList::new));
        IssueDemandSystemDistributionVO otherSystem = new IssueDemandSystemDistributionVO();
        otherSystem.setSystemType(0);
        otherSystem.setSystemTypeText("其他");
        otherSystem.setCount(systemTypeGroupCount.getOrDefault(0, 0L));
        result.add(otherSystem);
        result.sort(Comparator.comparing(IssueDemandSystemDistributionVO::getCount).reversed());
        return result;
    }

    /**
     * 获取职员提出数TOP
     */
    public List<IssueDemandProposerTopVO> getProposerTop(IssueDemandAnalysisQuery query) {
        LocalDateTime[] timeRange = query.buildTimeRange();
        CmtIssueDemandMapper baseMapper = (CmtIssueDemandMapper) issueDemandService.getBaseMapper();
        return baseMapper.getProposerTop(timeRange[0], timeRange[1]);
    }

    public List<IssueDemandProposeDeptTopVO> getProposeDeptTop(IssueDemandAnalysisQuery query) {
        LocalDateTime[] timeRange = query.buildTimeRange();
        CmtIssueDemandMapper baseMapper = (CmtIssueDemandMapper) issueDemandService.getBaseMapper();
        List<IssueDemandProposeDeptTopVO> data = baseMapper.getProposeDeptTop(timeRange[0], timeRange[1]);
        if (CollUtil.isNotEmpty(data)) {
            data.forEach(item -> {
                String effectiveRateText = item.getEffectiveRate()
                        .multiply(BigDecimal.valueOf(100))
                        .stripTrailingZeros()
                        .toPlainString() + "%";
                item.setEffectiveRateText(effectiveRateText);
            });
        }
        return data;
    }

    /**
     * 获取负责人任务统计
     */
    public List<IssueDemandPrincipalStatisticsVO> getPrincipalStatistics(IssueDemandAnalysisQuery query) {
        LocalDateTime[] timeRange = query.buildTimeRange();
        List<CmtIssueDemand> issueDemands = issueDemandService.lambdaQuery()
                .ge(CmtIssueDemand::getCreateTime, timeRange[0])
                .le(CmtIssueDemand::getCreateTime, timeRange[1])
                .isNotNull(CmtIssueDemand::getPrincipalUserId)
                .list();
        if (issueDemands.isEmpty()) {
            return List.of();
        }
        // 根据负责人分组
        Map<String, List<CmtIssueDemand>> principalGroup = issueDemands.stream().collect(Collectors.groupingBy(CmtIssueDemand::getPrincipalUserId));
        return principalGroup.entrySet().stream().map(item -> {
            String principalUserId = item.getKey();
            List<CmtIssueDemand> records = item.getValue();
            String principalUserName = records.getFirst().getPrincipalUserName();
            long total = records.size();
            long unfinishedTotal = records.stream()
                    .filter(v -> CmtLocalConstants.ISSUE_DEMAND_STATUS.PROCESSING.contains(v.getStatus()))
                    .count();
            long overdueTotal = records.stream().filter(v -> Objects.equals(computedDevIsOverdue(v), 1)).count();
            BigDecimal completionRate = BigDecimal.valueOf(total - unfinishedTotal).divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP);
            String completionRateText = completionRate
                    .multiply(BigDecimal.valueOf(100))
                    .stripTrailingZeros()
                    .toPlainString() + "%";

            IssueDemandPrincipalStatisticsVO vo = new IssueDemandPrincipalStatisticsVO();
            vo.setPrincipalUserId(principalUserId);
            vo.setPrincipalUserName(principalUserName);
            vo.setTotal(total);
            vo.setUnfinishedTotal(unfinishedTotal);
            vo.setOverdueTotal(overdueTotal);
            vo.setCompletionRate(completionRate);
            vo.setCompletionRateText(completionRateText);
            return vo;
        }).toList();

    }


    private Integer computedDevIsOverdue(CmtIssueDemand item) {
        if (Objects.isNull(item) || Objects.isNull(item.getPlanFinishTime())) {
            return null;
        }

        Integer status = item.getStatus();
        LocalDateTime planFinishTime = item.getPlanFinishTime();

        // 已完成 / 待验收：用实际完成时间判断
        if (CmtLocalConstants.ISSUE_DEMAND_STATUS.COMPLETED.equals(status)
                || CmtLocalConstants.ISSUE_DEMAND_STATUS.PENDING_ACCEPT.equals(status)) {

            if (Objects.isNull(item.getActualFinishTime())) {
                return null;
            }

            return item.getActualFinishTime().isAfter(planFinishTime) ? 1 : 0;
        }

        // 开发中：用当前时间判断
        if (CmtLocalConstants.ISSUE_DEMAND_STATUS.IN_PROGRESS.equals(status)) {
            return LocalDateTime.now().isAfter(planFinishTime) ? 1 : 0;
        }

        return null;
    }

    private Duration computedDevCostTime(CmtIssueDemand issueDemand) {
        if (CmtLocalConstants.ISSUE_DEMAND_STATUS.COMPLETED.equals(issueDemand.getStatus()) ||
                CmtLocalConstants.ISSUE_DEMAND_STATUS.PENDING_ACCEPT.equals(issueDemand.getStatus())) {
            // 开发耗时
            if (Objects.nonNull(issueDemand.getDevStartTime()) && Objects.nonNull(issueDemand.getActualFinishTime())) {
                return LocalDateTimeUtil.between(issueDemand.getDevStartTime(), issueDemand.getActualFinishTime());
            }
        }
        if (CmtLocalConstants.ISSUE_DEMAND_STATUS.VOIDED.equals(issueDemand.getStatus())) {
            // 开发耗时
            if (Objects.nonNull(issueDemand.getDevStartTime()) && Objects.nonNull(issueDemand.getVoidedTime())) {
                return LocalDateTimeUtil.between(issueDemand.getDevStartTime(), issueDemand.getVoidedTime());
            }
        }
        return null;
    }

    /**
     * 效率分析
     */
    public IssueDemandOverdueEfficiencyAnalysisVO getEfficiencyAnalysis(IssueDemandAnalysisQuery query) {
        LocalDateTime[] timeRange = query.buildTimeRange();
        List<CmtIssueDemand> issueDemands = issueDemandService.lambdaQuery()
                .ge(CmtIssueDemand::getCreateTime, timeRange[0])
                .le(CmtIssueDemand::getCreateTime, timeRange[1])
                .list();
        if (issueDemands.isEmpty()) {
            return new IssueDemandOverdueEfficiencyAnalysisVO();
        }
        long devOverdueTotal = issueDemands.stream().filter(v ->
                Objects.equals(v.getStatus(), CmtLocalConstants.ISSUE_DEMAND_STATUS.IN_PROGRESS)
                        && Objects.equals(this.computedDevIsOverdue(v), 1)).count();
        long acceptanceOverdueTotal = issueDemands.stream().filter(v -> Objects.equals(this.computedAcceptanceIsOverdue(v), 1)).count();
        Duration avgResponseDuration = this.computedAvgResponseDuration(issueDemands);
        Duration avgFinishCostDuration = this.computedAvgFinishCostDuration(issueDemands);
        IssueDemandOverdueEfficiencyAnalysisVO vo = new IssueDemandOverdueEfficiencyAnalysisVO();
        vo.setDevOverdueTotal(devOverdueTotal);
        vo.setAcceptanceOverdueTotal(acceptanceOverdueTotal);

        vo.setAvgResponseDurationText(CommonUtil.formatDuration(avgResponseDuration));
        vo.setAvgFinishCostDurationText(CommonUtil.formatDuration(avgFinishCostDuration));

        return vo;
    }

    private Duration computedFinishCostDuration(CmtIssueDemand item) {
        if (Objects.isNull(item) || Objects.isNull(item.getCreateTime())) {
            return null;
        }

        LocalDateTime createTime = item.getCreateTime();
        LocalDateTime endTime = null;

        Integer status = item.getStatus();

        // 已完成：创建时间 -> 实际完成时间
        if (CmtLocalConstants.ISSUE_DEMAND_STATUS.COMPLETED.equals(status)) {
            endTime = item.getActualFinishTime();
        }

        // 已作废：创建时间 -> 作废时间
        if (CmtLocalConstants.ISSUE_DEMAND_STATUS.VOIDED.equals(status)) {
            endTime = item.getVoidedTime();
        }

        if (Objects.isNull(endTime)) {
            return null;
        }

        return Duration.between(createTime, endTime);
    }

    private Duration computedAvgFinishCostDuration(List<CmtIssueDemand> records) {
        if (CollUtil.isEmpty(records)) {
            return Duration.ZERO;
        }

        Duration totalDuration = Duration.ZERO;
        long validCount = 0L;

        for (CmtIssueDemand item : records) {
            Duration duration = this.computedFinishCostDuration(item);
            if (Objects.isNull(duration)) {
                continue;
            }

            totalDuration = totalDuration.plus(duration);
            validCount++;
        }

        if (validCount == 0) {
            return Duration.ZERO;
        }

        return totalDuration.dividedBy(validCount);
    }
}
