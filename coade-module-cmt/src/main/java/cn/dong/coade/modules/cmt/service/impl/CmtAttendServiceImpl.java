package cn.dong.coade.modules.cmt.service.impl;

import cn.dong.coade.modules.cmt.constants.CmtLocalConstants;
import cn.dong.coade.modules.cmt.domain.bo.AttendRuleBO;
import cn.dong.coade.modules.cmt.domain.bo.CmtLoginUser;
import cn.dong.coade.modules.cmt.domain.bo.EkpAttendBusinessBO;
import cn.dong.coade.modules.cmt.domain.dto.*;
import cn.dong.coade.modules.cmt.domain.entity.CmtAttendReissue;
import cn.dong.coade.modules.cmt.domain.entity.CmtLeaveRequest;
import cn.dong.coade.modules.cmt.domain.entity.CmtOutgoingRequest;
import cn.dong.coade.modules.cmt.domain.entity.CmtUser;
import cn.dong.coade.modules.cmt.domain.enums.AttendRuleType;
import cn.dong.coade.modules.cmt.domain.query.AttendOutgoingDurationQuery;
import cn.dong.coade.modules.cmt.domain.vo.*;
import cn.dong.coade.modules.cmt.mapper.CmtAttendMapper;
import cn.dong.coade.modules.cmt.mapper.CmtUserMapper;
import cn.dong.coade.modules.cmt.service.*;
import cn.dong.coade.modules.cmt.utils.AttendRecordCalculator;
import cn.dong.coade.modules.cmt.utils.WeComApiUtil;
import cn.dong.nexus.common.constants.ApiConstants;
import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.core.api.ApiMessage;
import cn.dong.nexus.core.exception.BizException;
import cn.dong.nexus.core.security.context.IAuthContext;
import cn.dong.nexus.core.security.context.LoginUser;
import cn.dong.nexus.infra.util.DynamicDataSourceUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.baomidou.mybatisplus.extension.toolkit.SqlRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CmtAttendServiceImpl implements ICmtAttendService {
    private final IAuthContext authContext;
    private final CmtAttendMapper cmtAttendMapper;
    private final CmtUserMapper cmtUserMapper;
    private final ICmtUserService cmtUserService;
    private final AttendRecordCalculator attendRecordCalculator;
    private final ICmtAttendReissueService attendReissueService;
    private final RestTemplate restTemplate;
    private final ICmtAttendRuleService attendRuleService;
    private final CmtEkpService cmtEkpService;
    private final ICmtLeaveRequestService leaveRequestService;
    private final ICmtOutgoingRequestService outgoingRequestService;
    private static final String ATTEND_REISSUE_EKP_REVIEW_TEMPLATE_ID = "16be9d5fc79ef23244153e6457b9483a";


    /**
     * 获取用户今日企微打卡记录
     */
    @Override
    public UserAttendInfoVO getUserTodayAttend() {
        LocalDate today = LocalDate.now();
        return SpringUtil.getBean(this.getClass()).getUserAttendByDate(today.getYear(), today.getMonthValue(), today.getDayOfMonth());
    }

    @Override
    @DS(GlobalConstants.DataSource.EKP_SQLSERVER)
    public UserAttendInfoVO getUserAttendByDate(int year, int month, int day) {
        long start = System.currentTimeMillis();
        // 仅支持查询2026年4月之后的考勤
        if (LocalDate.of(year, month, day).isBefore(LocalDate.of(2026, 4, 1))) {
            return new UserAttendInfoVO("无需打卡", List.of(), new UserLeaveAttendVO());
        }
        CmtLoginUser loginUser = (CmtLoginUser) authContext.getLoginUserOrThrow();
        String weComId = loginUser.getExtInfo().get("weComId").toString();
        String ekpId = loginUser.getExtInfo().get("ekpId").toString();
        LocalDate now = LocalDate.of(year, month, day);
        LocalDateTime todayBegin = LocalDateTimeUtil.beginOfDay(now);
        LocalDateTime todayEnd = LocalDateTimeUtil.endOfDay(now);
        List<UserAttendRecordVO> userAttend = WeComApiUtil.getUserAttend(weComId, todayBegin, todayEnd);
        // 获取用户打卡规则
        AttendRuleBO rule = attendRuleService.getUserAttendRule(weComId, now);
//        String[][] range = {{"08:00", "11:30"}, {"12:30", "17:30"}};
//        EkpAttendRuleBO rule =   new EkpAttendRuleBO(range,new int[]{1,2,3,4,5,6},AttendRuleType.FIXED);
        if (Objects.isNull(rule)) {
            userAttend.forEach(item -> item.setStatus("正常"));
            return new UserAttendInfoVO("无需打卡", userAttend, new UserLeaveAttendVO());
        }

        String ruleInfo = this.buildRuleInfoText(rule);
        // 未关联蓝凌的用户
        if (GlobalConstants.UserIdentity.SPECIAL.equals(loginUser.getIdentity())) {
            userAttend = attendRecordCalculator.calculate(
                    now,
                    userAttend,
                    rule,
                    List.of(),
                    List.of(),
                    List.of()
            );
            return new UserAttendInfoVO(ruleInfo, userAttend, new UserLeaveAttendVO());
        }

        // 查询用户今天的补卡记录
        List<CmtAttendReissue> attendReissues = DynamicDataSourceUtil.switchTo(GlobalConstants.DataSource.LOCAL_MYSQL,
                () -> attendReissueService.lambdaQuery()
                        .eq(CmtAttendReissue::getEkpUserId, ekpId)
                        // 只需要处理中或通过的记录
                        .ne(CmtAttendReissue::getIsApproved, GlobalConstants.AttendReissueApprovalResult.REJECTED)
                        .between(CmtAttendReissue::getRuleCheckinTime, todayBegin, todayEnd)
                        .list());
        if (!attendReissues.isEmpty()) {
            Map<LocalDateTime, Integer> reissueRecordsMap = attendReissues.stream().collect(Collectors.toMap(CmtAttendReissue::getCheckinTime, CmtAttendReissue::getIsApproved));
            // 这里要把补卡通过的打卡记录过滤掉，因为补卡是新增一条规则打卡记录
            userAttend = userAttend.stream().filter(item -> {
                if (item.getIsReissue() == 1) {
                    return true;
                }
                Integer approveStatus = reissueRecordsMap.get(LocalDateTimeUtil.parse(item.getCheckinTime(), "yyyy-MM-dd HH:mm"));
                if (Objects.isNull(approveStatus)) {
                    return true;
                }
                // 如果有补卡记录，并且审批通过了，则过滤掉这条打卡记录
                return !GlobalConstants.AttendReissueApprovalResult.APPROVED.equals(approveStatus);
            }).toList();
        }
        // 请假记录
        List<EkpAttendBusinessBO> leaveInfo = cmtAttendMapper.selectUserEkpAttendBusiness(ekpId, todayBegin, todayEnd, GlobalConstants.EkpLeaveBizType.LEAVE);
//        EkpAttendBusinessBO r = new EkpAttendBusinessBO();
//        r.setStartTime(LocalDateTime.of(2026, 4, 8, 15, 0));
//        r.setEndTime(LocalDateTime.of(2026, 4, 8, 17, 30));
//        List<EkpAttendBusinessBO> leaveInfo = List.of(r);
        // 外出记录
        List<EkpAttendBusinessBO> outInfo = cmtAttendMapper.selectUserEkpAttendBusiness(ekpId, todayBegin, todayEnd, GlobalConstants.EkpLeaveBizType.OUTGOING);
        // 出差记录
        List<EkpAttendBusinessBO> tripInfo = cmtAttendMapper.selectUserEkpAttendBusiness(ekpId, todayBegin, todayEnd, GlobalConstants.EkpLeaveBizType.BIZ_TRIP);

        UserLeaveAttendVO userLeaveAttendVO = attendRecordCalculator.buildUserTodayLeaveInfo(leaveInfo, outInfo, tripInfo);
        userAttend = attendRecordCalculator.calculate(
                now,
                userAttend,
                rule,
                leaveInfo,
                outInfo,
                tripInfo
        );

        // 将补卡记录的审批结果应用到打卡记录上
        if (!attendReissues.isEmpty()) {
            Map<LocalDateTime, Integer> reissueRecordsMap = attendReissues.stream().collect(Collectors.toMap(CmtAttendReissue::getRuleCheckinTime, CmtAttendReissue::getIsApproved));

            userAttend.forEach(record -> {
                LocalDateTime getRuleCheckinTime = LocalDateTimeUtil.parse(record.getRuleCheckinTime(), "yyyy-MM-dd HH:mm");
                if (reissueRecordsMap.containsKey(getRuleCheckinTime)) {
                    Integer isApproved = reissueRecordsMap.get(getRuleCheckinTime);
                    record.setExceptionStatus(isApproved);
                }
            });
        }
        long end = System.currentTimeMillis();
        log.info("用户：{} 获取考勤耗时：{}，考勤日期：{}", loginUser.getUsername(), end - start, LocalDateTimeUtil.format(now, GlobalConstants.DatePattern.NORMAL_ONLY_DATE));

        return new UserAttendInfoVO(ruleInfo, userAttend, userLeaveAttendVO);

    }

    @Override
    public BigDecimal getCurrentUserLeaveDuration(LocalDateTime beginTime, LocalDateTime endTime) {
        CmtLoginUser loginUser = (CmtLoginUser) authContext.getLoginUser();
        return this.getLeaveDurationByEkpUserId(loginUser.getEkpId(), beginTime, endTime);
    }

    @Override
    public BigDecimal getLeaveDurationByEkpUserId(String ekpUserId, LocalDateTime beginTime, LocalDateTime endTime) {
        CmtUser user = cmtUserService.lambdaQuery().eq(CmtUser::getEkpId, ekpUserId).one();
        if (Objects.isNull(user)) {
            throw new BizException(ApiMessage.USER_NOT_FOUND);
        }
        return this.calculateDurationOfAttend(user.getWeComId(), beginTime, endTime);
    }

    @Override
    public List<UserAttendRecordVO> getMonthAbnormal(Integer month) {
        LoginUser loginUser = authContext.getLoginUserOrThrow();
        int year = LocalDate.now().getYear();

        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate today = LocalDate.now();

        if (monthStart.isBefore(LocalDate.of(2026, 4, 1))) {
            return List.of();
        }

        // 未来月份直接返回空，避免把未来工作日全算成缺卡
        LocalDate currentMonthStart = today.withDayOfMonth(1);
        if (monthStart.isAfter(currentMonthStart)) {
            return List.of();
        }

        // 当前月只统计到今天，避免未来日期被误判为异常
        LocalDate queryEndDate = monthStart.getYear() == today.getYear()
                && monthStart.getMonthValue() == today.getMonthValue()
                ? today
                : monthStart.plusMonths(1).minusDays(1);

        LocalDateTime begin = monthStart.atStartOfDay();
        LocalDateTime end = LocalDateTime.of(queryEndDate, LocalTime.of(23, 59, 59));

        String weComId = loginUser.getExtInfo().get("weComId").toString();

        // 用户该月所有原始打卡
        List<UserAttendRecordVO> records = WeComApiUtil.getUserAttend(weComId, begin, end);

        // 用户该月每日的考勤规则 map，key = day
        Map<Integer, AttendRuleBO> dayRuleMap = attendRuleService.getUserAttendRuleByMonth(weComId, year, month);
        if (CollUtil.isEmpty(dayRuleMap)) {
            return List.of();
        }

        List<EkpAttendBusinessBO> leaveInfo = List.of();
        List<EkpAttendBusinessBO> tripInfo = List.of();
        List<EkpAttendBusinessBO> outInfo = List.of();
        List<CmtAttendReissue> attendReissues = List.of();

        // 未关联蓝凌的用户，沿用“当天考勤”的思路：不查业务记录和补卡记录
        if (!GlobalConstants.UserIdentity.SPECIAL.equals(loginUser.getIdentity())) {
            String ekpId = Objects.toString(loginUser.getExtInfo().get("ekpId"), null);
            if (StrUtil.isNotBlank(ekpId)) {
                CmtAttendServiceImpl _this = SpringUtil.getBean(this.getClass());

                leaveInfo = _this.getAttendBizRecords(ekpId, begin, end, GlobalConstants.EkpLeaveBizType.LEAVE);
                tripInfo = _this.getAttendBizRecords(ekpId, begin, end, GlobalConstants.EkpLeaveBizType.BIZ_TRIP);
                outInfo = _this.getAttendBizRecords(ekpId, begin, end, GlobalConstants.EkpLeaveBizType.OUTGOING);

                attendReissues = attendReissueService.getUserReissueRecordsByTimeRange(ekpId, begin, end);
            }
        }

        Map<LocalDate, List<UserAttendRecordVO>> dayRecordMap = CollUtil.emptyIfNull(records).stream()
                .filter(item -> StrUtil.isNotBlank(item.getCheckinTime()))
                .collect(Collectors.groupingBy(
                        item -> LocalDateTimeUtil.parse(item.getCheckinTime(), "yyyy-MM-dd HH:mm").toLocalDate()
                ));

        List<UserAttendRecordVO> result = new ArrayList<>();

        for (LocalDate day = monthStart; !day.isAfter(queryEndDate); day = day.plusDays(1)) {
            AttendRuleBO rule = dayRuleMap.get(day.getDayOfMonth());

            if (Objects.isNull(rule)) {
                rule = attendRuleService.getUserAttendRule(weComId, day);
                if (Objects.isNull(rule) || AttendRuleType.EMPTY.equals(rule.getRuleType())) {
                    continue;
                }
            }

            List<UserAttendRecordVO> dayActualRecords = new ArrayList<>(
                    dayRecordMap.getOrDefault(day, Collections.emptyList())
            );

            List<CmtAttendReissue> dayReissues = filterReissuesByDay(attendReissues, day);

            // 先过滤掉“补卡审批通过后生成的原始打卡”
            dayActualRecords = removeApprovedReissueGeneratedPunch(dayActualRecords, dayReissues);

            List<EkpAttendBusinessBO> dayLeaveInfo = filterBizByDay(leaveInfo, day);
            List<EkpAttendBusinessBO> dayTripInfo = filterBizByDay(tripInfo, day);
            List<EkpAttendBusinessBO> dayOutInfo = filterBizByDay(outInfo, day);

            List<UserAttendRecordVO> dayCalculated = attendRecordCalculator.calculate(
                    day,
                    dayActualRecords,
                    rule,
                    dayLeaveInfo,
                    dayOutInfo,
                    dayTripInfo
            );

            // 再把补卡审批状态回填到规则打卡点
            applyReissueStatus(dayCalculated, dayReissues);

            dayCalculated.stream()
                    .filter(this::isAbnormalAttendRecord)
                    .forEach(result::add);
        }

        result.sort(Comparator.comparing(this::resolveSortTime));

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addLeaveRequest(AttendLeaveRequestDTO dto) {
        CmtLoginUser loginUser = (CmtLoginUser) authContext.getLoginUserOrThrow();
        dto.doValidate();
        // 计算请假时长
        BigDecimal duration = this.calculateDurationOfAttend(loginUser.getWeComId(), dto.getBeginTime(), dto.getEndTime());
        dto.setDuration(duration);
        CmtLeaveRequest entity = dto.toEntity();
        entity.setUserEkpId(loginUser.getEkpId());
        entity.setUserId(loginUser.getId());
        leaveRequestService.save(entity);
        // 发起EKP请假流程审批
        String ekpReviewId = cmtEkpService.startLeaveRequestReview(dto, loginUser);
        leaveRequestService.lambdaUpdate().eq(CmtLeaveRequest::getId, entity.getId())
                .set(CmtLeaveRequest::getEkpReviewId, ekpReviewId)
                .update();
    }

    @Override
    public void saveOrUpdateLeaveRequestStatus(AttendLeaveRequestEkpCallbackDTO dto) {
        // 查询是否本系统提交的申请
        CmtLeaveRequest leaveRequest = leaveRequestService.lambdaQuery().eq(CmtLeaveRequest::getEkpReviewId, dto.getEkpReviewId()).one();
        if (Objects.nonNull(leaveRequest)) {
            // 更新请假申请状态
            leaveRequestService.lambdaUpdate()
                    .set(CmtLeaveRequest::getStatus, dto.getStatus())
                    .eq(CmtLeaveRequest::getId, leaveRequest.getId())
                    .update();
            return;
        }
        if (CmtLocalConstants.ATTEND_REQUEST_STATUS.APPROVED.equals(dto.getStatus())) {
            // 新增请假申请
            CmtUser user = cmtUserService.lambdaQuery().eq(CmtUser::getEkpId, dto.getUserEkpId()).one();
            if (Objects.isNull(user)) {
                throw new BizException("CMT用户不存在,userEkpId:" + dto.getUserEkpId());
            }
            CmtLeaveRequest record = BeanUtil.copyProperties(dto, CmtLeaveRequest.class);
            record.setUserId(user.getId());
            record.setCreateBy(user.getId());
            leaveRequestService.save(record);
        }
    }

    @Override
    public List<AttendLeaveRequestVO> getUserLeaveRequestList() {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        List<CmtLeaveRequest> leaveRequests = leaveRequestService.lambdaQuery()
                .eq(CmtLeaveRequest::getUserId, authContext.getLoginUserId())
                .ge(CmtLeaveRequest::getCreateTime, sixMonthsAgo)
                .orderByDesc(CmtLeaveRequest::getCreateTime)
                .list();
        return BeanUtil.copyToList(leaveRequests, AttendLeaveRequestVO.class);

    }

    @Override
    @DSTransactional(rollbackFor = Exception.class)
    public void revokeLeaveRequest(String id) {
        CmtLeaveRequest leaveRequest = leaveRequestService.getById(id);
        if (Objects.isNull(leaveRequest)) {
            throw new BizException(ApiMessage.NOT_FOUND);
        }
        if (CmtLocalConstants.ATTEND_REQUEST_STATUS.APPROVED.equals(leaveRequest.getStatus())) {
            throw new BizException("请假申请已审批通过，无法撤销！");
        }
        cmtEkpService.deleteEkpReview(leaveRequest.getEkpReviewId());
        leaveRequestService.lambdaUpdate().set(CmtLeaveRequest::getStatus, CmtLocalConstants.ATTEND_REQUEST_STATUS.REVOKED)
                .eq(CmtLeaveRequest::getId, id)
                .update();
    }

    @Override
    public BigDecimal getOutgoingDurationByEkpUserId(AttendOutgoingDurationQuery query) {
        if (Objects.isNull(query.getOutTimeEnd()) || Objects.isNull(query.getOutTimeBegin())) {
            return BigDecimal.ZERO;
        }
        LocalDateTime beginTime = query.getOutDate().atTime(query.getOutTimeBegin());
        LocalDateTime endTime = query.getOutDate().atTime(query.getOutTimeEnd());
        CmtUser user = cmtUserService.lambdaQuery().eq(CmtUser::getEkpId, query.getUserEkpId()).one();
        if (Objects.isNull(user)) {
            return BigDecimal.ZERO;
        }
        return this.calculateDurationOfAttend(user.getWeComId(), beginTime, endTime);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addOutgoingRequest(AttendOutgoingRequestDTO dto) {
        CmtLoginUser loginUser = (CmtLoginUser) authContext.getLoginUserOrThrow();
        dto.doValidate();
        // 计算请假时长
        LocalDateTime beginTime = dto.getOutDate().atTime(dto.getOutTimeBegin());
        LocalDateTime endTime = dto.getOutDate().atTime(dto.getOutTimeEnd());
        BigDecimal duration = this.calculateDurationOfAttend(loginUser.getWeComId(), beginTime, endTime);
        dto.setDuration(duration);
        CmtOutgoingRequest entity = dto.toEntity();
        entity.setUserEkpId(loginUser.getEkpId());
        entity.setUserId(loginUser.getId());
        outgoingRequestService.save(entity);
        // 发起EKP请假流程审批
        String ekpReviewId = cmtEkpService.startOutgoingRequestReview(dto, loginUser);
        outgoingRequestService.lambdaUpdate().eq(CmtOutgoingRequest::getId, entity.getId())
                .set(CmtOutgoingRequest::getEkpReviewId, ekpReviewId)
                .update();
    }

    @Override
    public void saveOrUpdateOutgoingRequestStatus(AttendOutgoingRequestEkpCallbackDTO dto) {
        // 查询是否本系统提交的申请
        CmtOutgoingRequest outgoing = outgoingRequestService.lambdaQuery().eq(CmtOutgoingRequest::getEkpReviewId, dto.getEkpReviewId()).one();
        if (Objects.nonNull(outgoing)) {
            // 更新外出申请状态
            outgoingRequestService.lambdaUpdate()
                    .set(CmtOutgoingRequest::getStatus, dto.getStatus())
                    .eq(CmtOutgoingRequest::getId, outgoing.getId())
                    .update();
            return;
        }
        if (CmtLocalConstants.ATTEND_REQUEST_STATUS.APPROVED.equals(dto.getStatus())) {
            // 新增外出申请
            CmtUser user = cmtUserService.lambdaQuery().eq(CmtUser::getEkpId, dto.getUserEkpId()).one();
            if (Objects.isNull(user)) {
                throw new BizException("CMT用户不存在,userEkpId:" + dto.getUserEkpId());
            }
            CmtOutgoingRequest record = BeanUtil.copyProperties(dto, CmtOutgoingRequest.class);
            record.setUserId(user.getId());
            record.setCreateBy(user.getId());
            outgoingRequestService.save(record);
        }
    }

    @Override
    public BigDecimal getCurrentUserOutgoingDuration(AttendOutgoingDurationQuery query) {
        CmtLoginUser loginUser = (CmtLoginUser) authContext.getLoginUser();
        query.setUserEkpId(loginUser.getEkpId());
        return this.getOutgoingDurationByEkpUserId(query);
    }

    @Override
    public List<AttendOutgoingRequestVO> getUserOutgoingRequestList() {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        List<CmtOutgoingRequest> outgoingRequests = outgoingRequestService.lambdaQuery()
                .eq(CmtOutgoingRequest::getUserId, authContext.getLoginUserId())
                .ge(CmtOutgoingRequest::getCreateTime, sixMonthsAgo)
                .orderByDesc(CmtOutgoingRequest::getCreateTime)
                .list();
        return BeanUtil.copyToList(outgoingRequests, AttendOutgoingRequestVO.class);
    }

    @Override
    public void revokeOutgoingRequest(String id) {
        CmtOutgoingRequest outgoingRequest = outgoingRequestService.getById(id);
        if (Objects.isNull(outgoingRequest)) {
            throw new BizException(ApiMessage.NOT_FOUND);
        }
        if (CmtLocalConstants.ATTEND_REQUEST_STATUS.APPROVED.equals(outgoingRequest.getStatus())) {
            throw new BizException("请假申请已审批通过，无法撤销！");
        }
        cmtEkpService.deleteEkpReview(outgoingRequest.getEkpReviewId());
        outgoingRequestService.lambdaUpdate().set(CmtOutgoingRequest::getStatus, CmtLocalConstants.ATTEND_REQUEST_STATUS.REVOKED)
                .eq(CmtOutgoingRequest::getId, id)
                .update();
    }

    /**
     * 计算请假工时
     */
    @Override
    public BigDecimal calculateDurationOfAttend(String weComId, LocalDateTime beginTime, LocalDateTime endTime) {
        if (StrUtil.isBlank(weComId) || Objects.isNull(beginTime) || Objects.isNull(endTime) || !endTime.isAfter(beginTime)) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalMinutes = BigDecimal.ZERO;
        LocalDate currentDate = beginTime.toLocalDate();
        LocalDate endDate = endTime.toLocalDate();

        while (!currentDate.isAfter(endDate)) {
            // 逐天获取当天考勤规则
            AttendRuleBO rule = attendRuleService.getUserAttendRule(weComId, currentDate);
            if (rule == null || rule.getRuleType() == AttendRuleType.EMPTY) {
                currentDate = currentDate.plusDays(1);
                continue;
            }

            // 获取“用于计算请假时长”的时间段
            String[][] leaveCalcRanges = getLeaveCalcRanges(rule);

            for (String[] range : leaveCalcRanges) {
                LocalDateTime workStart = LocalDateTime.of(currentDate, LocalTime.parse(range[0]));
                LocalDateTime workEnd = LocalDateTime.of(currentDate, LocalTime.parse(range[1]));

                // 请假区间与工作区间求交集
                LocalDateTime actualStart = beginTime.isAfter(workStart) ? beginTime : workStart;
                LocalDateTime actualEnd = endTime.isBefore(workEnd) ? endTime : workEnd;

                if (actualEnd.isAfter(actualStart)) {
                    long minutes = Duration.between(actualStart, actualEnd).toMinutes();
                    totalMinutes = totalMinutes.add(BigDecimal.valueOf(minutes));
                }
            }

            currentDate = currentDate.plusDays(1);
        }

        return totalMinutes
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP)
                .stripTrailingZeros();
    }

    /**
     * 获取“用于计算请假时长”的班次区间
     */
    private String[][] getLeaveCalcRanges(AttendRuleBO rule) {
        if (rule.getRuleType() == AttendRuleType.EMPTY) {
            return new String[0][];
        }

        if (rule.getRuleType() == AttendRuleType.IMD) {
            // 注塑部请假时长按这个规则计算，不按原始打卡点规则算
            return new String[][]{
                    {"08:00", "11:15"},
                    {"11:45", "17:30"},
                    {"18:00", "20:30"}
            };
        }

        // FIXED 直接按原始规则算
        return rule.getTimeRanges() == null ? new String[0][] : rule.getTimeRanges();
    }


    private List<EkpAttendBusinessBO> filterBizByDay(List<EkpAttendBusinessBO> source, LocalDate day) {
        if (CollUtil.isEmpty(source)) {
            return List.of();
        }

        LocalDateTime dayBegin = day.atStartOfDay();
        LocalDateTime dayEnd = LocalDateTime.of(day, LocalTime.of(23, 59, 59));

        return source.stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getStartTime() != null && item.getEndTime() != null)
                // 与当天有交集即可
                .filter(item -> !item.getEndTime().isBefore(dayBegin) && !item.getStartTime().isAfter(dayEnd))
                .collect(Collectors.toList());
    }

    private List<CmtAttendReissue> filterReissuesByDay(List<CmtAttendReissue> source, LocalDate day) {
        if (CollUtil.isEmpty(source)) {
            return List.of();
        }

        return source.stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getRuleCheckinTime() != null)
                .filter(item -> item.getRuleCheckinTime().toLocalDate().equals(day))
                .collect(Collectors.toList());
    }

    private List<UserAttendRecordVO> removeApprovedReissueGeneratedPunch(List<UserAttendRecordVO> userAttend,
                                                                         List<CmtAttendReissue> attendReissues) {
        if (CollUtil.isEmpty(userAttend) || CollUtil.isEmpty(attendReissues)) {
            return userAttend;
        }

        Map<LocalDateTime, Integer> reissueRecordsMap = attendReissues.stream()
                .filter(item -> item.getCheckinTime() != null)
                .collect(Collectors.toMap(
                        CmtAttendReissue::getCheckinTime,
                        CmtAttendReissue::getIsApproved,
                        (a, b) -> a
                ));

        return userAttend.stream()
                .filter(item -> {
                    if (Objects.equals(item.getIsReissue(), 1)) {
                        return true;
                    }
                    if (StrUtil.isBlank(item.getCheckinTime())) {
                        return true;
                    }

                    Integer approveStatus = reissueRecordsMap.get(
                            LocalDateTimeUtil.parse(item.getCheckinTime(), "yyyy-MM-dd HH:mm")
                    );

                    if (Objects.isNull(approveStatus)) {
                        return true;
                    }

                    return !GlobalConstants.AttendReissueApprovalResult.APPROVED.equals(approveStatus);
                })
                .collect(Collectors.toList());
    }

    private void applyReissueStatus(List<UserAttendRecordVO> records, List<CmtAttendReissue> attendReissues) {
        if (CollUtil.isEmpty(records) || CollUtil.isEmpty(attendReissues)) {
            return;
        }

        Map<LocalDateTime, Integer> reissueRecordsMap = attendReissues.stream()
                .filter(item -> item.getRuleCheckinTime() != null)
                .collect(Collectors.toMap(
                        CmtAttendReissue::getRuleCheckinTime,
                        CmtAttendReissue::getIsApproved,
                        (a, b) -> a
                ));

        records.forEach(record -> {
            if (StrUtil.isBlank(record.getRuleCheckinTime())) {
                return;
            }

            LocalDateTime ruleCheckinTime = LocalDateTimeUtil.parse(record.getRuleCheckinTime(), "yyyy-MM-dd HH:mm");
            Integer status = reissueRecordsMap.get(ruleCheckinTime);
            if (status != null) {
                record.setExceptionStatus(status);
            }
        });
    }

    private boolean isAbnormalAttendRecord(UserAttendRecordVO record) {
        if (record == null || StrUtil.isBlank(record.getStatus())) {
            return false;
        }

        return StrUtil.equalsAny(record.getStatus(),
                "迟到",
                "早退",
                "缺卡",
                "上班缺卡",
                "下班缺卡");
    }

    private LocalDateTime resolveSortTime(UserAttendRecordVO record) {
        if (record != null && StrUtil.isNotBlank(record.getRuleCheckinTime())) {
            return LocalDateTimeUtil.parse(record.getRuleCheckinTime(), "yyyy-MM-dd HH:mm");
        }
        if (record != null && StrUtil.isNotBlank(record.getCheckinTime())) {
            return LocalDateTimeUtil.parse(record.getCheckinTime(), "yyyy-MM-dd HH:mm");
        }
        return LocalDateTime.MIN;
    }

    @DS(GlobalConstants.DataSource.EKP_SQLSERVER)
    public List<EkpAttendBusinessBO> getAttendBizRecords(String ekpId, LocalDateTime begin, LocalDateTime end, Integer bizType) {
        return cmtAttendMapper.selectUserEkpAttendBusiness(ekpId, begin, end, bizType);
    }

    private String buildRuleInfoText(AttendRuleBO rule) {
        String[][] timeRanges = rule.getTimeRanges();
        return Arrays.stream(timeRanges)
                .map(range -> {
                    String start = range[0];
                    String end = range[1];
                    if (start.equals(end)) {
                        return start;
                    }
                    return StrUtil.format("{}-{}", range[0], range[1]);
                })
                .collect(Collectors.joining(", "));
    }


    @Override
    public String getUserAttendRule(String ekpId) {
        // 1) 先查用户是否有“直接考勤组”
        String groupName = cmtAttendMapper.selectOrgAttendGroupName(ekpId);
        if (StrUtil.isNotBlank(groupName)) {
            return groupName;
        }

        // 2) 再沿着部门/组织往上找（最多 3 层）
        String currentId = cmtUserMapper.selectEkpOrgParentId(ekpId);
        for (int level = 0; level < 3 && StrUtil.isNotBlank(currentId); level++) {

            groupName = cmtAttendMapper.selectOrgAttendGroupName(currentId);
            if (StrUtil.isNotBlank(groupName)) {
                return groupName;
            }

            // 继续往上
            String nextId = cmtUserMapper.selectEkpOrgParentId(currentId);

            // 防止到顶/脏数据导致自循环
            if (StrUtil.isBlank(nextId) || StrUtil.equals(nextId, currentId)) {
                break;
            }
            currentId = nextId;
        }

        throw new BizException("未找到用户的考勤规则，请联系管理员配置考勤组");
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reissueAttendApply(ReissueAttendDTO dto) {
        CmtUser cmtUser = cmtUserMapper.selectById(dto.getCmtUserId());
        if (Objects.isNull(cmtUser)) {
            throw new BizException(ApiMessage.USER_NOT_FOUND);
        }
        DateTime checkinDate = DateUtil.parse(dto.getRuleCheckinTime(), "yyyy-MM-dd HH:mm");
        DateTime monthBegin = DateUtil.beginOfMonth(checkinDate);
        DateTime monthEnd = DateUtil.endOfMonth(checkinDate);
        Long count = attendReissueService.lambdaQuery()
                .eq(CmtAttendReissue::getEkpUserId, cmtUser.getEkpId())
                .between(CmtAttendReissue::getRuleCheckinTime, monthBegin, monthEnd)
                .ne(CmtAttendReissue::getIsApproved, GlobalConstants.AttendReissueApprovalResult.REJECTED)
                .count();
        if (count >= 3) {
            throw new BizException("当月补卡次数已用完!");
        }
        boolean exists = attendReissueService.lambdaQuery().eq(CmtAttendReissue::getCmtUserId, dto.getCmtUserId())
                .eq(CmtAttendReissue::getRuleCheckinTime, dto.getRuleCheckinTime())
                .ne(CmtAttendReissue::getIsApproved, GlobalConstants.AttendReissueApprovalResult.REJECTED)
                .exists();
        if (exists) {
            throw new BizException("该考勤异常记录正在处理，请勿重复提交！");
        }
        // 本地创建补卡申请记录
        CmtAttendReissue attendReissue = BeanUtil.copyProperties(dto, CmtAttendReissue.class);
        attendReissue.setEkpUserId(cmtUser.getEkpId());

        // 向EKP 发起审批
        String ekpReviewId = this.initiateReissueToEkpReview(dto, cmtUser);

        attendReissue.setEkpReviewId(ekpReviewId);
        attendReissueService.save(attendReissue);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reissueAttendApplyForLoginUser(ReissueAttendDTO dto) {
        LoginUser loginUser = authContext.getLoginUserOrThrow();
        dto.setCmtUserId(loginUser.getId());
        this.reissueAttendApply(dto);
    }

    @Override
    @DSTransactional(rollbackFor = Exception.class)
    public void doReissueAttend(AttendReissueApplyPassDTO dto) {
        CmtAttendReissue attendReissue = attendReissueService.lambdaQuery().eq(CmtAttendReissue::getEkpReviewId, dto.getEkpReviewId()).one();
        if (Objects.isNull(attendReissue)) {
            log.error("未找到对应的补卡申请记录，ekpReviewId={}", dto.getEkpReviewId());
            throw new BizException(ApiMessage.INTERNAL_ERROR);
        }
        if (GlobalConstants.INT_YES.equals(attendReissue.getIsApproved())) {
            return;
        }
        CmtUser cmtUser = cmtUserMapper.selectById(attendReissue.getCmtUserId());
        if (Objects.isNull(cmtUser)) {
            log.error("未找到补卡申请的cmt用户，cmtUserId={}", attendReissue.getCmtUserId());
            return;
        }
        // 审批通过 添加企微补卡记录
        if (GlobalConstants.AttendReissueApprovalResult.APPROVED.equals(dto.getIsApproved())) {
            WeComApiUtil.addUserAttend(cmtUser.getWeComId(), attendReissue.getRuleCheckinTime());
        } else {
            // 审批被驳回 将蓝凌的审批流程删除
            this.deleteReissueProcessForEkp(dto.getEkpReviewId());
        }
        attendReissueService.lambdaUpdate()
                .set(CmtAttendReissue::getIsApproved, dto.getIsApproved())
                .eq(CmtAttendReissue::getEkpReviewId, dto.getEkpReviewId())
                .update();
    }

    @Override
    public Integer getUsedReissueFrequency(String cmtUserId, Integer year, Integer month) {
        CmtUser cmtUser = cmtUserMapper.selectById(cmtUserId);
        if (Objects.isNull(cmtUser)) {
            throw new BizException(ApiMessage.USER_NOT_FOUND);
        }
        DateTime date = DateUtil.parse(StrUtil.format("{}-{}-01", year, month), "yyyy-MM-dd");
        DateTime monthBegin = DateUtil.beginOfMonth(date);
        DateTime monthEnd = DateUtil.endOfMonth(date);
        return attendReissueService.lambdaQuery()
                .eq(CmtAttendReissue::getEkpUserId, cmtUser.getEkpId())
                .between(CmtAttendReissue::getRuleCheckinTime, monthBegin, monthEnd)
                .ne(CmtAttendReissue::getIsApproved, GlobalConstants.AttendReissueApprovalResult.REJECTED)
                .count().intValue();
    }


    private void deleteReissueProcessForEkp(String ekpReviewId) {
        DynamicDataSourceContextHolder.push(GlobalConstants.DataSource.EKP_SQLSERVER);
        SqlRunner.db().delete("delete FROM km_review_main_areader WHERE fd_doc_id = {0}", ekpReviewId);
        SqlRunner.db().delete("delete FROM km_review_main_oreader WHERE fd_doc_id = {0}", ekpReviewId);
        SqlRunner.db().delete("delete FROM km_review_main WHERE fd_id = {0}", ekpReviewId);
        DynamicDataSourceContextHolder.poll();
    }

    private String initiateReissueToEkpReview(ReissueAttendDTO dto, CmtUser cmtUser) {
        String docSubject = StrUtil.format("{}的打卡异常处理申请", cmtUser.getUsername());
        String docCreator = StrUtil.format("""
                {"Id":"{}"}
                """, cmtUser.getEkpId());
        String formValues = StrUtil.format("""
                    {
                     "fd_3eb701f7efa0f8":"{}",
                     "fd_3eb7020398c5c4":"{}",
                     "fd_3ceda385caa300":"{}",
                     "fd_3efc2f191ad740":"{}"
                    }
                """, dto.getCheckinTime(), dto.getReissueType(), dto.getReason(), dto.getRuleCheckinTime());
        MultiValueMap<String, Object> wholeForm = new LinkedMultiValueMap<>();
        wholeForm.add("docSubject", docSubject);
        wholeForm.add("docCreator", docCreator);
        wholeForm.add("docStatus", 20);
        wholeForm.add("fdTemplateId", ATTEND_REISSUE_EKP_REVIEW_TEMPLATE_ID);
        wholeForm.add("formValues", formValues);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(wholeForm, headers);

        String ekpBaseUrl = SpringUtil.getProperty("coade.ekp.server-url");
        String url = ekpBaseUrl + ApiConstants.INITIATE_EKP_REVIEW;

        ResponseEntity<String> resp;
        resp = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        if (Objects.isNull(resp.getBody()) || StrUtil.isBlank(resp.getBody())) {
            log.error("发起补卡申请到EKP审批失败，EKP接口返回异常，url={}, body={}", url, resp.getBody());
            throw new BizException(ApiMessage.INTERNAL_ERROR);
        }
        log.info("发起补卡申请到EKP审批成功，url={}, body={}", url, resp.getBody());
        return resp.getBody();
    }

    public void test() throws IOException {
        // 6S整改标题
        String docSubject = StrUtil.format("测试推送6S整改");
        // 创建人
        String docCreator = new JSONObject().set("Id", "190e86d8c6e297f712af1224f19abacf").toJSONString(1);
        JSONObject content = new JSONObject();
        // 责任部门
        content.set("fd_3e8b05b852e42c", new JSONObject().set("Id", "197aac24cfce7199955ad114b5483bbc"));
        // 责任人
        content.set("fd_3e8b05c3b915ce", new JSONObject().set("Id", "190e86d8c6e297f712af1224f19abacf"));

        MultiValueMap<String, Object> wholeForm = new LinkedMultiValueMap<>();

        // 整改项
        JSONArray items = new JSONArray();
        int attIndex = 0;
        for (int i = 0; i < 4; i++) {
            int imgCount = 2;

            JSONArray imgAttKeys = new JSONArray();
            JSONObject item = new JSONObject()
                    // 整改内容
                    .set("fd_3e8b057dd5931c.fd_3e8b06cf4a9d4c", i)
                    // 截止日期
                    .set("fd_3e8b057dd5931c.fd_3e8b06d20587fe", "2026-03-13")
                    // 协助人
                    .set("fd_3e8b057dd5931c.fd_3e8b08373a1ea4", new JSONObject().set("Id", "190e86d8c6e297f712af1224f19abacf"))
                    // 问题照片
                    .set("fd_3e8b057dd5931c.fd_3e8b05f375483e", imgAttKeys);
            items.add(item);
            for (int j = 0; j < imgCount; j++) {
                String attKey = UUID.fastUUID().toString(true);
                imgAttKeys.set(attKey);
                String attForm = StrUtil.format("attachmentForms[{}]", attIndex++);
                wholeForm.add(attForm + ".fdKey", attKey);
                wholeForm.add(attForm + ".fdFileName", StrUtil.format("{}.png", RandomUtil.randomString(5)));
                wholeForm.add(attForm + ".fdAttachment", new FileSystemResource(new File("D:\\upload\\20260206\\95cd11a0deaaa79f.png")));
            }
        }
        content.set("fd_3e8b057dd5931c", items);
        wholeForm.add("docSubject", docSubject);
        wholeForm.add("docCreator", docCreator);
        wholeForm.add("docStatus", 20);
        wholeForm.add("fdTemplateId", "199e1d2c5cff3ef9e9b53a346f0ab173");
        wholeForm.add("formValues", content.toJSONString(1));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(wholeForm, headers);

        String ekpBaseUrl = SpringUtil.getProperty("coade.ekp.server-url");
        String url = ekpBaseUrl + ApiConstants.INITIATE_EKP_REVIEW;

        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        String body = exchange.getBody();
        System.out.println();
    }


//    JSONObject formValues = new JSONObject();
//        formValues.set("fd_3e8b05b852e42c", new JSONObject().set("Id", "197aac24cfce7199955ad114b5483bbc"));
//        formValues.set("fd_3e8b05c3b915ce", new JSONObject().set("Id", "190e86d8c6e297f712af1224f19abacf"));
//        formValues.set("fd_3e8b084f325fb8", "备注");
//
//        formValues.set("fd_3e8b057dd5931c",
//                new JSONArray().put(new JSONObject()
//                        .set("fd_3e8b06cf4a9d4c", "整改项")
//                        .set("fd_3e8b06d20587fe", "截至时间")
//                        .set("fd_3e8b08373a1ea4", new JSONObject().set("Id", "190e86d8c6e297f712af1224f19abacf"))));
//    String jsonPrettyStr = JSONUtil.toJsonPrettyStr(formValues);

}
