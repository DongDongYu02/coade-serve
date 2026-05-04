package cn.dong.coade.modules.cmt.service.impl;

import cn.dong.coade.modules.cmt.constants.CmtLocalConstants;
import cn.dong.coade.modules.cmt.domain.bo.AttendDurationBO;
import cn.dong.coade.modules.cmt.domain.bo.AttendRuleBO;
import cn.dong.coade.modules.cmt.domain.bo.CmtLoginUser;
import cn.dong.coade.modules.cmt.domain.bo.EkpAttendBusinessBO;
import cn.dong.coade.modules.cmt.domain.dto.*;
import cn.dong.coade.modules.cmt.domain.entity.*;
import cn.dong.coade.modules.cmt.domain.enums.AttendRuleType;
import cn.dong.coade.modules.cmt.domain.query.AttendBizTripDurationQuery;
import cn.dong.coade.modules.cmt.domain.query.AttendMonthDataQuery;
import cn.dong.coade.modules.cmt.domain.query.AttendOutgoingDurationQuery;
import cn.dong.coade.modules.cmt.domain.query.AttendOvertimeDurationQuery;
import cn.dong.coade.modules.cmt.domain.vo.*;
import cn.dong.coade.modules.cmt.mapper.CmtAttendMapper;
import cn.dong.coade.modules.cmt.mapper.CmtUserMapper;
import cn.dong.coade.modules.cmt.service.*;
import cn.dong.coade.modules.cmt.support.*;
import cn.dong.coade.modules.cmt.utils.WeComApiUtil;
import cn.dong.nexus.common.api.FileExportCommonApi;
import cn.dong.nexus.common.constants.ApiConstants;
import cn.dong.nexus.common.constants.AttachmentOwnerType;
import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.common.domain.bo.FileExportBO;
import cn.dong.nexus.common.domain.vo.FileExportVO;
import cn.dong.nexus.core.api.ApiMessage;
import cn.dong.nexus.core.exception.BizException;
import cn.dong.nexus.core.resmapping.ResMappingUtil;
import cn.dong.nexus.core.security.context.IAuthContext;
import cn.dong.nexus.core.security.context.LoginUser;
import cn.dong.nexus.core.util.FesodExcelUtil;
import cn.dong.nexus.core.util.PageUtil;
import cn.dong.nexus.core.util.UploadUtil;
import cn.dong.nexus.infra.util.DynamicDataSourceUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.toolkit.SqlRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
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
    private final ICmtBizTripRequestService bizTripRequestService;
    private final ICmtOvertimeRequestService overtimeRequestService;
    private final FileExportCommonApi fileExportCommonApi;
    private final AttendExportService attendExportService;
    private static final String ATTEND_REISSUE_EKP_REVIEW_TEMPLATE_ID = "16be9d5fc79ef23244153e6457b9483a";

    private static final Set<LocalDate> noNeedCheckinDates = Set.of(
            LocalDate.of(2026, 4, 4),
            LocalDate.of(2026, 4, 5)
    );


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
        LocalDate now = LocalDate.of(year, month, day);
        // 仅支持查询2026年4月之后的考勤
        if (now.isBefore(LocalDate.of(2026, 4, 1))) {
            return new UserAttendInfoVO("无需打卡", List.of(), new UserLeaveAttendVO());
        }
        CmtLoginUser loginUser = (CmtLoginUser) authContext.getLoginUserOrThrow();
        String weComId = loginUser.getExtInfo().get("weComId").toString();
        String ekpId = loginUser.getExtInfo().get("ekpId").toString();
        LocalDateTime todayBegin = LocalDateTimeUtil.beginOfDay(now);
        LocalDateTime todayEnd = LocalDateTimeUtil.endOfDay(now);
        List<UserAttendRecordVO> userAttend = WeComApiUtil.getUserAttend(List.of(weComId), todayBegin, todayEnd);
        // 获取用户打卡规则
        AttendRuleBO rule = attendRuleService.getUserAttendRule(weComId, now);
//        String[][] range = {{"08:00", "11:30"}, {"12:30", "17:30"}};
//        EkpAttendRuleBO rule =   new EkpAttendRuleBO(range,new int[]{1,2,3,4,5,6},AttendRuleType.FIXED);
        if (Objects.isNull(rule) || AttendRuleType.EMPTY.equals(rule.getRuleType()) || noNeedCheckinDates.contains(now)) {
            userAttend.forEach(item -> item.setStatus("正常"));
            UserLeaveAttendVO leaveAttendVO = new UserLeaveAttendVO();
            if (!GlobalConstants.UserIdentity.SPECIAL.equals(loginUser.getIdentity())) {
                List<EkpAttendBusinessBO> overtimeInfo = cmtAttendMapper.selectUserEkpAttendBusiness(ekpId, todayBegin, todayEnd, GlobalConstants.EkpLeaveBizType.OVERTIME);
                leaveAttendVO.setOvertimeTimes(attendRecordCalculator.formatOvertimeTimes(overtimeInfo));
            }
            return new UserAttendInfoVO("无需打卡", userAttend, leaveAttendVO);
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
        // 加班记录
        List<EkpAttendBusinessBO> overtimeInfo = cmtAttendMapper.selectUserEkpAttendBusiness(ekpId, todayBegin, todayEnd, GlobalConstants.EkpLeaveBizType.OVERTIME);

        UserLeaveAttendVO userLeaveAttendVO = attendRecordCalculator.buildUserTodayLeaveInfo(leaveInfo, outInfo, tripInfo, overtimeInfo);
        userAttend = attendRecordCalculator.calculate(
                now,
                userAttend,
                rule,
                leaveInfo,
                outInfo,
                tripInfo,
                overtimeInfo,
                Collections.emptySet()
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
    public AttendDurationVO getCurrentUserLeaveDuration(LocalDateTime beginTime, LocalDateTime endTime) {
        CmtLoginUser loginUser = (CmtLoginUser) authContext.getLoginUser();
        return this.getAttendDurationByEkpUserId(loginUser.getEkpId(), beginTime, endTime);
    }

    @Override
    public AttendDurationVO getAttendDurationByEkpUserId(String ekpUserId, LocalDateTime beginTime, LocalDateTime endTime) {
        CmtUser user = cmtUserService.lambdaQuery().eq(CmtUser::getEkpId, ekpUserId).one();
        if (Objects.isNull(user)) {
            throw new BizException(ApiMessage.USER_NOT_FOUND);
        }
        AttendDurationBO durationBO = this.calculateDurationOfAttend(user.getWeComId(), beginTime, endTime);
        return BeanUtil.copyProperties(durationBO, AttendDurationVO.class);
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
        List<UserAttendRecordVO> records = WeComApiUtil.getUserAttend(List.of(weComId), begin, end);

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
            if (noNeedCheckinDates.contains(day)) {
                continue;
            }
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
        AttendDurationBO durationBO = this.calculateDurationOfAttend(loginUser.getWeComId(), dto.getBeginTime(), dto.getEndTime());
        dto.setDuration(durationBO.getDuration());
        dto.setDurationFormat(durationBO.getDurationFormat());
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
            if (CmtLocalConstants.ATTEND_REQUEST_STATUS.PENDING.equals(dto.getStatus())) {
                return;
            }
            // 更新请假申请状态
            leaveRequestService.lambdaUpdate()
                    .set(CmtLeaveRequest::getStatus, dto.getStatus())
                    .eq(CmtLeaveRequest::getId, leaveRequest.getId())
                    .update();
            return;
        }
        // 新增请假申请
        CmtUser user = cmtUserService.lambdaQuery().eq(CmtUser::getEkpId, dto.getUserEkpId()).one();
        if (Objects.isNull(user)) {
            throw new BizException("CMT用户不存在,userEkpId:" + dto.getUserEkpId());
        }
        // 判断请假区间内是否已有请假申请
        boolean exists = leaveRequestService.lambdaQuery()
                .in(CmtLeaveRequest::getStatus, CmtLocalConstants.ATTEND_REQUEST_STATUS.PENDING, CmtLocalConstants.ATTEND_REQUEST_STATUS.APPROVED)
                .le(CmtLeaveRequest::getBeginTime, dto.getEndTime())
                .ge(CmtLeaveRequest::getEndTime, dto.getBeginTime())
                .eq(CmtLeaveRequest::getUserId, user.getId())
                .exists();
        if (exists) {
            throw new BizException("选择的时间段内已经提交过申请了！");
        }
        CmtLeaveRequest record = BeanUtil.copyProperties(dto, CmtLeaveRequest.class);
        record.setUserId(user.getId());
        record.setStatus(dto.getStatus());
        record.setCreateBy(user.getId());
        leaveRequestService.save(record);
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
    public AttendDurationVO getOutgoingDurationByEkpUserId(AttendOutgoingDurationQuery query) {
        if (Objects.isNull(query.getOutTimeEnd()) || Objects.isNull(query.getOutTimeBegin())) {
            return new AttendDurationVO(BigDecimal.ZERO, "0小时");
        }
        LocalDateTime beginTime = query.getOutDate().atTime(query.getOutTimeBegin());
        LocalDateTime endTime = query.getOutDate().atTime(query.getOutTimeEnd());
        CmtUser user = cmtUserService.lambdaQuery().eq(CmtUser::getEkpId, query.getUserEkpId()).one();
        if (Objects.isNull(user)) {
            return new AttendDurationVO(BigDecimal.ZERO, "0小时");
        }
        AttendDurationBO durationBO = this.calculateDurationOfAttend(user.getWeComId(), beginTime, endTime);
        return new AttendDurationVO(durationBO.getDuration(), durationBO.getDurationFormat());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addOutgoingRequest(AttendOutgoingRequestDTO dto) {
        CmtLoginUser loginUser = (CmtLoginUser) authContext.getLoginUserOrThrow();
        dto.doValidate();
        // 计算请假时长
        LocalDateTime beginTime = dto.getOutDate().atTime(dto.getOutTimeBegin());
        LocalDateTime endTime = dto.getOutDate().atTime(dto.getOutTimeEnd());
        BigDecimal duration = this.calculateDurationOfAttend(loginUser.getWeComId(), beginTime, endTime).getDuration();
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
        // 新增外出申请
        CmtUser user = cmtUserService.lambdaQuery().eq(CmtUser::getEkpId, dto.getUserEkpId()).one();
        if (Objects.isNull(user)) {
            throw new BizException("CMT用户不存在,userEkpId:" + dto.getUserEkpId());
        }
        // 判断外出区间内是否已有申请
        boolean exists = outgoingRequestService.lambdaQuery()
                .in(CmtOutgoingRequest::getStatus, CmtLocalConstants.ATTEND_REQUEST_STATUS.PENDING, CmtLocalConstants.ATTEND_REQUEST_STATUS.APPROVED)
                .le(CmtOutgoingRequest::getOutTimeBegin, dto.getOutTimeEnd())
                .ge(CmtOutgoingRequest::getOutTimeEnd, dto.getOutTimeBegin())
                .eq(CmtOutgoingRequest::getOutDate, dto.getOutDate())
                .eq(CmtOutgoingRequest::getUserId, user.getId())
                .exists();
        if (exists) {
            throw new BizException("选择的时间段内已经提交过申请了！");
        }
        CmtOutgoingRequest record = BeanUtil.copyProperties(dto, CmtOutgoingRequest.class);
        record.setUserId(user.getId());
        record.setStatus(dto.getStatus());
        record.setCreateBy(user.getId());
        outgoingRequestService.save(record);
    }

    @Override
    public BigDecimal getCurrentUserOutgoingDuration(AttendOutgoingDurationQuery query) {
        CmtLoginUser loginUser = (CmtLoginUser) authContext.getLoginUser();
        query.setUserEkpId(loginUser.getEkpId());
        return this.getOutgoingDurationByEkpUserId(query).getDuration();
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addBizTripRequest(AttendBizTripRequestDTO dto) {
        CmtLoginUser loginUser = (CmtLoginUser) authContext.getLoginUserOrThrow();
        dto.doValidate();
        // 计算出差时长
        AttendDurationBO duration = this.calculateDays(dto.getBeginTime(), dto.getEndTime());
        dto.setDuration(duration.getDuration());
        dto.setDurationFormat(duration.getDurationFormat());
        CmtBizTripRequest entity = dto.toEntity();
        entity.setUserEkpId(loginUser.getEkpId());
        entity.setUserId(loginUser.getId());
        bizTripRequestService.save(entity);
        // 发起EKP出差流程审批
        String ekpReviewId = cmtEkpService.startBizTripRequestReview(dto, loginUser);
        bizTripRequestService.lambdaUpdate().eq(CmtBizTripRequest::getId, entity.getId())
                .set(CmtBizTripRequest::getEkpReviewId, ekpReviewId)
                .update();
    }

    @Override
    public void saveOrUpdateBizTripRequestStatus(AttendBizTripRequestEkpCallbackDTO dto) {
        // 查询是否本系统提交的申请
        CmtBizTripRequest bizTripRequest = bizTripRequestService.lambdaQuery().eq(CmtBizTripRequest::getEkpReviewId, dto.getEkpReviewId()).one();
        if (Objects.nonNull(bizTripRequest)) {
            // 更新外出申请状态
            bizTripRequestService.lambdaUpdate()
                    .set(CmtBizTripRequest::getStatus, dto.getStatus())
                    .eq(CmtBizTripRequest::getId, bizTripRequest.getId())
                    .update();
            return;
        }
        // 新增出差申请
        CmtUser user = cmtUserService.lambdaQuery().eq(CmtUser::getEkpId, dto.getUserEkpId()).one();
        if (Objects.isNull(user)) {
            throw new BizException("CMT用户不存在,userEkpId:" + dto.getUserEkpId());
        }
        // 判断请假区间内是否已有请假申请
        boolean exists = bizTripRequestService.lambdaQuery()
                .in(CmtBizTripRequest::getStatus, CmtLocalConstants.ATTEND_REQUEST_STATUS.PENDING, CmtLocalConstants.ATTEND_REQUEST_STATUS.APPROVED)
                .le(CmtBizTripRequest::getBeginTime, dto.getEndTime())
                .ge(CmtBizTripRequest::getEndTime, dto.getBeginTime())
                .eq(CmtBizTripRequest::getUserId, user.getId())
                .exists();
        if (exists) {
            throw new BizException("选择的出差时间段内已经提交过申请了！");
        }
        CmtBizTripRequest record = BeanUtil.copyProperties(dto, CmtBizTripRequest.class);
        record.setUserId(user.getId());
        record.setCreateBy(user.getId());
        record.setStatus(dto.getStatus());
        bizTripRequestService.save(record);
    }

    @Override
    public List<AttendBizTripRequestVO> getUserBizTripRequestList() {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        List<CmtBizTripRequest> bizTripRequests = bizTripRequestService.lambdaQuery()
                .eq(CmtBizTripRequest::getUserId, authContext.getLoginUserId())
                .ge(CmtBizTripRequest::getCreateTime, sixMonthsAgo)
                .orderByDesc(CmtBizTripRequest::getCreateTime)
                .list();
        return BeanUtil.copyToList(bizTripRequests, AttendBizTripRequestVO.class);
    }

    @Override
    public void revokeBizTripRequest(String id) {
        CmtBizTripRequest bizTripRequest = bizTripRequestService.getById(id);
        if (Objects.isNull(bizTripRequest)) {
            throw new BizException(ApiMessage.NOT_FOUND);
        }
        if (CmtLocalConstants.ATTEND_REQUEST_STATUS.APPROVED.equals(bizTripRequest.getStatus())) {
            throw new BizException("出差申请已审批通过，无法撤销！");
        }
        cmtEkpService.deleteEkpReview(bizTripRequest.getEkpReviewId());
        bizTripRequestService.lambdaUpdate().set(CmtBizTripRequest::getStatus, CmtLocalConstants.ATTEND_REQUEST_STATUS.REVOKED)
                .eq(CmtBizTripRequest::getId, id)
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addOvertimeRequest(AttendOvertimeRequestDTO dto) {
        CmtLoginUser loginUser = (CmtLoginUser) authContext.getLoginUserOrThrow();
        dto.doValidate();
        OvertimeDurationCalculator calculator =
                new OvertimeDurationCalculator(attendRuleService::getUserAttendRule);
        LocalDateTime beginTime = dto.getOvertimeDate().atTime(dto.getBeginTime());
        LocalDateTime endTime = dto.getOvertimeDate().atTime(dto.getEndTime());
        AttendDurationBO durationBO = calculator.calculateDurationOfOvertime(loginUser.getWeComId(), beginTime, endTime, noNeedCheckinDates);
        if (durationBO.getDuration().compareTo(BigDecimal.valueOf(0.5)) < 0) {
            throw new BizException("加班时长必须超过半小时!");
        }
        dto.setDuration(durationBO.getDuration());
        CmtOvertimeRequest entity = dto.toEntity();
        entity.setUserEkpId(loginUser.getEkpId());
        entity.setUserId(loginUser.getId());
        overtimeRequestService.save(entity);
        // 发起EKP加班流程审批
        String ekpReviewId = cmtEkpService.startOvertimeRequestReview(dto, loginUser);
        overtimeRequestService.lambdaUpdate().eq(CmtOvertimeRequest::getId, entity.getId())
                .set(CmtOvertimeRequest::getEkpReviewId, ekpReviewId)
                .update();
    }

    @Override
    public BigDecimal getOvertimeDurationByEkpUserId(AttendOvertimeDurationQuery query) {
        if (Objects.isNull(query.getBeginTime()) || Objects.isNull(query.getEndTime())) {
            return BigDecimal.ZERO;
        }
        CmtUser user = cmtUserService.lambdaQuery().eq(CmtUser::getEkpId, query.getUserEkpId()).one();
        if (Objects.isNull(user)) {
            return BigDecimal.ZERO;
        }
        OvertimeDurationCalculator calculator =
                new OvertimeDurationCalculator(attendRuleService::getUserAttendRule);
        LocalDateTime beginTime = query.getOvertimeDate().atTime(query.getBeginTime());
        LocalDateTime endTime = query.getOvertimeDate().atTime(query.getEndTime());
        return calculator.calculateDurationOfOvertime(user.getWeComId(), beginTime, endTime, noNeedCheckinDates).getDuration();
    }

    @Override
    public void saveOrUpdateOvertimeRequestStatus(AttendOvertimeRequestEkpCallbackDTO dto) {
        // 查询是否本系统提交的申请
        CmtOvertimeRequest overtimeRequest = overtimeRequestService.lambdaQuery().eq(CmtOvertimeRequest::getEkpReviewId, dto.getEkpReviewId()).one();
        if (Objects.nonNull(overtimeRequest)) {
            // 更新加班申请状态
            overtimeRequestService.lambdaUpdate()
                    .set(CmtOvertimeRequest::getStatus, dto.getStatus())
                    .eq(CmtOvertimeRequest::getId, overtimeRequest.getId())
                    .update();
            return;
        }
        // 新增加班申请
        if (dto.getDuration().compareTo(BigDecimal.valueOf(0.5)) < 0) {
            throw new BizException("加班时长必须超过半小时!");
        }
        CmtUser user = cmtUserService.lambdaQuery().eq(CmtUser::getEkpId, dto.getUserEkpId()).one();
        if (Objects.isNull(user)) {
            throw new BizException("CMT用户不存在,userEkpId:" + dto.getUserEkpId());
        }
        // 判断区间内是否已有申请
        boolean exists = overtimeRequestService.lambdaQuery()
                .in(CmtOvertimeRequest::getStatus, CmtLocalConstants.ATTEND_REQUEST_STATUS.PENDING, CmtLocalConstants.ATTEND_REQUEST_STATUS.APPROVED)
                .le(CmtOvertimeRequest::getBeginTime, dto.getEndTime())
                .ge(CmtOvertimeRequest::getEndTime, dto.getBeginTime())
                .eq(CmtOvertimeRequest::getOvertimeDate, dto.getOvertimeDate())
                .eq(CmtOvertimeRequest::getUserId, user.getId())
                .exists();
        if (exists) {
            throw new BizException("选择的时间段内已经提交过申请了！");
        }
        CmtOvertimeRequest record = BeanUtil.copyProperties(dto, CmtOvertimeRequest.class);
        record.setUserId(user.getId());
        record.setCreateBy(user.getId());
        record.setStatus(dto.getStatus());
        overtimeRequestService.save(record);
    }

    @Override
    public void revokeOvertimeRequest(String id) {
        CmtOvertimeRequest overtimeRequest = overtimeRequestService.getById(id);
        if (Objects.isNull(overtimeRequest)) {
            throw new BizException(ApiMessage.NOT_FOUND);
        }
        if (CmtLocalConstants.ATTEND_REQUEST_STATUS.APPROVED.equals(overtimeRequest.getStatus())) {
            throw new BizException("出差申请已审批通过，无法撤销！");
        }
        cmtEkpService.deleteEkpReview(overtimeRequest.getEkpReviewId());
        overtimeRequestService.lambdaUpdate().set(CmtOvertimeRequest::getStatus, CmtLocalConstants.ATTEND_REQUEST_STATUS.REVOKED)
                .eq(CmtOvertimeRequest::getId, id)
                .update();
    }

    @Override
    public BigDecimal getCurrentUserOvertimeDuration(AttendOvertimeDurationQuery query) {
        CmtLoginUser loginUser = (CmtLoginUser) authContext.getLoginUser();
        query.setUserEkpId(loginUser.getEkpId());
        return this.getOvertimeDurationByEkpUserId(query);
    }

    @Override
    public List<AttendOvertimeRequestVO> getUserOvertimeRequestList() {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        List<CmtOvertimeRequest> bizTripRequests = overtimeRequestService.lambdaQuery()
                .eq(CmtOvertimeRequest::getUserId, authContext.getLoginUserId())
                .ge(CmtOvertimeRequest::getCreateTime, sixMonthsAgo)
                .orderByDesc(CmtOvertimeRequest::getCreateTime)
                .list();
        return BeanUtil.copyToList(bizTripRequests, AttendOvertimeRequestVO.class);
    }

    @Override
    public IPage<AttendMonthDataVO> getUserMonthAttendData(AttendMonthDataQuery query) {
        LocalDate monthStart = LocalDate.of(query.getYear(), query.getMonth(), 1);
        LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);

        LocalDate today = LocalDate.now();

        LocalDateTime beginTime = monthStart.atStartOfDay();
        LocalDateTime endTime = LocalDateTime.of(monthEnd, LocalTime.of(23, 59, 59));


        IPage<CmtUser> userPage = cmtUserService.lambdaQuery()
                .eq(StrUtil.isNotBlank(query.getDept()), CmtUser::getDept, query.getDept())
                .in(CollUtil.isNotEmpty(query.getUserIds()), CmtUser::getId, query.getUserIds())
                .page(query.toPage());
        IPage<AttendMonthDataVO> page = PageUtil.convertPage(userPage, AttendMonthDataVO.class);
        List<AttendMonthDataVO> users = page.getRecords();
        if (CollUtil.isEmpty(users)) {
            throw new BizException(ApiMessage.USER_NOT_FOUND);
        }

        List<String> weComIds = users.stream()
                .map(AttendMonthDataVO::getWeComId)
                .toList();
        List<String> ekpIds = users.stream()
                .map(AttendMonthDataVO::getEkpId)
                .toList();

        List<UserAttendRecordVO> attendRecords = WeComApiUtil.getUserAttendByMonth(weComIds, query.getYear(), query.getMonth());
        Map<String, List<UserAttendRecordVO>> userAttendRecordMap = CollUtil.emptyIfNull(attendRecords).stream()
                .filter(item -> StrUtil.isNotBlank(item.getWeComId()))
                .collect(Collectors.groupingBy(UserAttendRecordVO::getWeComId));

        Map<String, Map<Integer, AttendRuleBO>> userRuleMap = attendRuleService.getUsersAttendRuleByMonth(
                weComIds,
                query.getYear(),
                query.getMonth()
        );

        CmtAttendServiceImpl _this = SpringUtil.getBean(this.getClass());
        List<EkpAttendBusinessBO> leaveInfo = _this.getUsersAttendBizRecords(ekpIds, beginTime, endTime, GlobalConstants.EkpLeaveBizType.LEAVE);
        List<EkpAttendBusinessBO> tripInfo = _this.getUsersAttendBizRecords(ekpIds, beginTime, endTime, GlobalConstants.EkpLeaveBizType.BIZ_TRIP);
        List<EkpAttendBusinessBO> outInfo = _this.getUsersAttendBizRecords(ekpIds, beginTime, endTime, GlobalConstants.EkpLeaveBizType.OUTGOING);
        List<EkpAttendBusinessBO> overtimeInfo = _this.getUsersAttendBizRecords(ekpIds, beginTime, endTime, GlobalConstants.EkpLeaveBizType.OVERTIME);
        List<CmtAttendReissue> attendReissues = attendReissueService.getUsersReissueRecordsByTimeRange(ekpIds, beginTime, endTime);

        Map<String, List<EkpAttendBusinessBO>> leaveInfoMap = groupBizRecordsByEkpId(leaveInfo);
        Map<String, List<EkpAttendBusinessBO>> tripInfoMap = groupBizRecordsByEkpId(tripInfo);
        Map<String, List<EkpAttendBusinessBO>> outInfoMap = groupBizRecordsByEkpId(outInfo);
        Map<String, List<EkpAttendBusinessBO>> overtimeInfoMap = groupBizRecordsByEkpId(overtimeInfo);
        Map<String, List<CmtAttendReissue>> attendReissueMap = groupReissueRecordsByEkpId(attendReissues);

        List<AttendMonthDataVO> result = new ArrayList<>();

        // 按入参 userIds 顺序返回
        for (AttendMonthDataVO user : users) {
            AttendMonthDataVO monthDataVO = buildSingleUserMonthAttendData(
                    user,
                    monthStart,
                    monthEnd,
                    today,
                    getRecordsByKey(userAttendRecordMap, user.getWeComId()),
                    getUserRuleMap(userRuleMap, user.getWeComId()),
                    getRecordsByKey(leaveInfoMap, user.getEkpId()),
                    getRecordsByKey(tripInfoMap, user.getEkpId()),
                    getRecordsByKey(outInfoMap, user.getEkpId()),
                    getRecordsByKey(overtimeInfoMap, user.getEkpId()),
                    getRecordsByKey(attendReissueMap, user.getEkpId())
            );

            result.add(monthDataVO);
        }

        if (CollUtil.isEmpty(result)) {
            throw new BizException(ApiMessage.USER_NOT_FOUND);
        }
        ResMappingUtil.translateField(result);
        page.setRecords(result);
        return page;
    }

    /**
     * 构建单个用户的月考勤数据。
     * <p>
     * 多用户查询时，每个用户独立计算，避免不同用户之间的：
     * 1. 打卡记录串数据
     * 2. 请假 / 外出 / 出差 / 加班记录串数据
     * 3. 补卡记录串数据
     * 4. 考勤规则串数据
     */
    private AttendMonthDataVO buildSingleUserMonthAttendData(AttendMonthDataVO monthDataVO,
                                                             LocalDate monthStart,
                                                             LocalDate monthEnd,
                                                             LocalDate today,
                                                             List<UserAttendRecordVO> checkinRecords,
                                                             Map<Integer, AttendRuleBO> dayRuleMap,
                                                             List<EkpAttendBusinessBO> leaveInfo,
                                                             List<EkpAttendBusinessBO> tripInfo,
                                                             List<EkpAttendBusinessBO> outInfo,
                                                             List<EkpAttendBusinessBO> overtimeInfo,
                                                             List<CmtAttendReissue> attendReissues) {
        Map<Integer, AttendRuleBO> userDayRuleMap = Objects.isNull(dayRuleMap)
                ? new HashMap<>()
                : new HashMap<>(dayRuleMap);
        // 4. 原始打卡记录按日期分组
        Map<LocalDate, List<UserAttendRecordVO>> dayRecordMap = CollUtil.emptyIfNull(checkinRecords)
                .stream()
                .filter(item -> StrUtil.isNotBlank(item.getCheckinTime()))
                .collect(Collectors.groupingBy(item ->
                        LocalDateTimeUtil.parse(item.getCheckinTime(), "yyyy-MM-dd HH:mm").toLocalDate()
                ));
        Map<LocalDate, List<EkpAttendBusinessBO>> leaveDayMap = groupBizRecordsByDay(leaveInfo, monthStart, monthEnd);
        Map<LocalDate, List<EkpAttendBusinessBO>> tripDayMap = groupBizRecordsByDay(tripInfo, monthStart, monthEnd);
        Map<LocalDate, List<EkpAttendBusinessBO>> outDayMap = groupBizRecordsByDay(outInfo, monthStart, monthEnd);
        Map<LocalDate, List<EkpAttendBusinessBO>> overtimeDayMap = groupBizRecordsByDay(overtimeInfo, monthStart, monthEnd);
        Map<LocalDate, List<CmtAttendReissue>> reissueDayMap = groupReissueRecordsByDay(attendReissues);

        List<AttendMonthDataVO.DayCase> dayCases = new ArrayList<>();

        // 出勤天数累计
        BigDecimal attendDays = BigDecimal.ZERO;

        // 请假天数累计
        BigDecimal leaveDays = BigDecimal.ZERO;

        // 出差天数累计，按自然日统计
        AttendDurationBO bizTripDuration = calculateMonthBizTripDays(
                tripInfo,
                monthStart,
                monthEnd
        );

        // 缺卡次数累计
        int shortages = 0;

        // 迟到次数累计
        int lateCount = 0;

        // 迟到时长累计，单位：分钟
        long lateDurationMinutes = 0L;

        // 早退次数累计
        int earlyCount = 0;

        // 早退时长累计，单位：分钟
        long earlyDurationMinutes = 0L;

        // 加班小时累计，单位：小时
        BigDecimal overtimeDuration = calculateMonthOvertimeDuration(
                monthDataVO.getWeComId(),
                overtimeInfo,
                monthStart.atStartOfDay(),
                monthStart.plusMonths(1).atStartOfDay(),
                monthStart,
                userDayRuleMap
        );

        // 实际出勤分钟数，用于格式化成 x天x小时x分钟
        long attendMinutes = 0L;

        // 请假分钟数，用于格式化成 x天x小时x分钟
        long leaveMinutes = 0L;

        // 用于格式化时判断“一天”等于多少分钟
        long formatStandardDayMinutes = 0L;

        // 5. 遍历本月每天
        for (LocalDate day = monthStart; !day.isAfter(monthEnd); day = day.plusDays(1)) {
            AttendMonthDataVO.DayCase dayCase = new AttendMonthDataVO.DayCase();
            dayCase.setDay(day.getDayOfMonth());

            List<String> dayData = new ArrayList<>();

            // 5.1 当天业务记录
            List<EkpAttendBusinessBO> dayLeaveInfo = leaveDayMap.getOrDefault(day, List.of());
            List<EkpAttendBusinessBO> dayTripInfo = tripDayMap.getOrDefault(day, List.of());
            List<EkpAttendBusinessBO> dayOutInfo = outDayMap.getOrDefault(day, List.of());
            List<EkpAttendBusinessBO> dayOvertimeInfo = overtimeDayMap.getOrDefault(day, List.of());

            // 先展示业务记录
            dayData.addAll(buildMonthBizTexts("请假", dayLeaveInfo));
            dayData.addAll(buildMonthBizTexts("出差", dayTripInfo));
            dayData.addAll(buildMonthBizTexts("外出", dayOutInfo));
            dayData.addAll(buildMonthBizTexts("加班", dayOvertimeInfo));

            // 未来日期不计算考勤，也不累计出勤天数
            if (day.isAfter(today)) {
                if (dayData.isEmpty()) {
                    dayData.add("未开始");
                }
                dayCase.setData(dayData);
                dayCases.add(dayCase);
                continue;
            }

            // 手动无需打卡日期
            if (noNeedCheckinDates.contains(day)) {
                if (dayData.isEmpty()) {
                    dayData.add("无需打卡");
                }
                dayCase.setData(dayData);
                dayCases.add(dayCase);
                continue;
            }

            // 5.2 获取当天考勤规则
            AttendRuleBO rule = getMonthRuleOrLoad(monthDataVO.getWeComId(), day, monthStart, userDayRuleMap);

            if (Objects.isNull(rule) || AttendRuleType.EMPTY.equals(rule.getRuleType())) {
                if (dayData.isEmpty()) {
                    dayData.add("无需打卡");
                }
                dayCase.setData(dayData);
                dayCases.add(dayCase);
                continue;
            }

            // 5.3 当天原始打卡记录
            List<UserAttendRecordVO> dayActualRecords = new ArrayList<>(
                    dayRecordMap.getOrDefault(day, Collections.emptyList())
            );

            // 5.4 当天补卡记录
            List<CmtAttendReissue> dayReissues = reissueDayMap.getOrDefault(day, List.of());

            // 先过滤掉“补卡审批通过后由企微新增的原始打卡”
            dayActualRecords = removeApprovedReissueGeneratedPunch(dayActualRecords, dayReissues);

            // 5.5 记录一个标准工作日分钟数，用于最后格式化
            long currentStandardMinutes = calculateStandardWorkMinutes(rule);
            if (formatStandardDayMinutes <= 0 && currentStandardMinutes > 0) {
                formatStandardDayMinutes = currentStandardMinutes;
            }

            // 5.6 计算当天请假天数和请假分钟数
            long dayLeaveMinutes = calculateDayLeaveMinutes(
                    dayLeaveInfo,
                    rule,
                    day
            );
            leaveMinutes += dayLeaveMinutes;

            BigDecimal dayLeaveDays = calculateDayLeaveDays(
                    dayLeaveMinutes,
                    currentStandardMinutes
            );
            leaveDays = leaveDays.add(dayLeaveDays);

            // 5.7 计算当天出勤天数
            BigDecimal dayAttendDays = calculateDayAttendDays(
                    dayActualRecords,
                    dayLeaveInfo,
                    dayOutInfo,
                    dayTripInfo,
                    rule,
                    day
            );
            attendDays = attendDays.add(dayAttendDays);

            // 5.8 累计实际出勤分钟数，用于格式化展示
            long dayAttendMinutes = calculateDayAttendMinutes(
                    dayActualRecords,
                    dayLeaveInfo,
                    dayOutInfo,
                    dayTripInfo,
                    rule,
                    day
            );
            attendMinutes += dayAttendMinutes;

            // 5.9 复用现有考勤计算器，得到当天完整打卡状态
            List<UserAttendRecordVO> dayCalculated = attendRecordCalculator.calculate(
                    day,
                    dayActualRecords,
                    rule,
                    dayLeaveInfo,
                    dayOutInfo,
                    dayTripInfo,
                    dayOvertimeInfo,
                    Collections.emptySet()
            );

            // 5.10 回填补卡审批状态
            applyReissueStatus(dayCalculated, dayReissues);

            // 5.11 累计缺卡 / 迟到 / 早退次数
            shortages += countShortageAttendRecords(dayCalculated);
            lateCount += countLateAttendRecords(dayCalculated);
            lateDurationMinutes += calculateLateDurationMinutes(dayCalculated);
            earlyCount += countEarlyAttendRecords(dayCalculated);
            earlyDurationMinutes += calculateEarlyDurationMinutes(dayCalculated);

            // 5.12 组装当天打卡文案
            dayCalculated.stream()
                    .sorted(Comparator.comparing(this::resolveSortTime))
                    .map(this::buildMonthAttendRecordText)
                    .filter(StrUtil::isNotBlank)
                    .forEach(dayData::add);

            if (dayData.isEmpty()) {
                dayData.add("无考勤数据");
            }

            dayCase.setData(dayData);
            dayCases.add(dayCase);
        }

        monthDataVO.setDayCases(dayCases);

        monthDataVO.setAttendDays(attendDays.setScale(2, RoundingMode.HALF_UP));
        monthDataVO.setAttendDaysText(formatAttendDurationMore(attendMinutes, formatStandardDayMinutes));

        monthDataVO.setLeaveDays(leaveDays.setScale(2, RoundingMode.HALF_UP));
        monthDataVO.setLeaveDaysText(formatAttendDurationMore(leaveMinutes, formatStandardDayMinutes));

        monthDataVO.setBizTripDays(bizTripDuration.getDuration());
        monthDataVO.setBizTripDaysText(formatBizTripDaysText(bizTripDuration));

        monthDataVO.setShortages(shortages);
        monthDataVO.setLateCount(lateCount);
        monthDataVO.setLateDuration(formatLateDuration(lateDurationMinutes));
        monthDataVO.setEarlyCount(earlyCount);
        monthDataVO.setEarlyDuration(formatEarlyDuration(earlyDurationMinutes));

        monthDataVO.setOvertimeDuration(formatOvertimeDuration(overtimeDuration));

        return monthDataVO;
    }

    /**
     * 统计缺卡次数。
     * <p>
     * 说明：
     * 1. 只统计明确的缺卡状态。
     * 2. 待打卡、未开始、请假、外出、出差、无需打卡不计入缺卡次数。
     */
    private int countShortageAttendRecords(List<UserAttendRecordVO> records) {
        if (CollUtil.isEmpty(records)) {
            return 0;
        }

        return (int) records.stream()
                .filter(Objects::nonNull)
                .filter(record -> StrUtil.equalsAny(
                        record.getStatus(),
                        "缺卡",
                        "上班缺卡",
                        "下班缺卡"
                ))
                .count();
    }

    /**
     * 统计迟到次数。
     */
    private int countLateAttendRecords(List<UserAttendRecordVO> records) {
        return countAttendRecordsByStatus(records, "迟到");
    }

    /**
     * 统计迟到时长，单位：分钟。
     * <p>
     * 说明：
     * 1. 只统计状态为“迟到”的记录。
     * 2. 使用实际打卡时间 checkinTime - 规则打卡时间 ruleCheckinTime。
     * 3. 如果时间缺失或实际时间不晚于规则时间，不累计。
     */
    private long calculateLateDurationMinutes(List<UserAttendRecordVO> records) {
        if (CollUtil.isEmpty(records)) {
            return 0L;
        }

        long totalMinutes = 0L;

        for (UserAttendRecordVO record : records) {
            if (Objects.isNull(record)
                    || !StrUtil.equals(record.getStatus(), "迟到")
                    || StrUtil.isBlank(record.getCheckinTime())
                    || StrUtil.isBlank(record.getRuleCheckinTime())) {
                continue;
            }

            LocalDateTime checkinTime = LocalDateTimeUtil.parse(record.getCheckinTime(), "yyyy-MM-dd HH:mm");
            LocalDateTime ruleCheckinTime = LocalDateTimeUtil.parse(record.getRuleCheckinTime(), "yyyy-MM-dd HH:mm");

            if (checkinTime.isAfter(ruleCheckinTime)) {
                totalMinutes += Duration.between(ruleCheckinTime, checkinTime).toMinutes();
            }
        }

        return totalMinutes;
    }

    /**
     * 格式化迟到时长。
     * <p>
     * 返回单位：分钟。
     * example:
     * 5
     * 80
     * 0
     */
    private Integer formatLateDuration(long totalMinutes) {
        if (totalMinutes <= 0) {
            return 0;
        }

        return Math.toIntExact(totalMinutes);
    }

    /**
     * 统计早退次数。
     */
    private int countEarlyAttendRecords(List<UserAttendRecordVO> records) {
        return countAttendRecordsByStatus(records, "早退");
    }

    /**
     * 统计早退时长，单位：分钟。
     * <p>
     * 说明：
     * 1. 只统计状态为“早退”的记录。
     * 2. 使用规则打卡时间 ruleCheckinTime - 实际打卡时间 checkinTime。
     * 3. 如果时间缺失或实际时间不早于规则时间，不累计。
     */
    private long calculateEarlyDurationMinutes(List<UserAttendRecordVO> records) {
        if (CollUtil.isEmpty(records)) {
            return 0L;
        }

        long totalMinutes = 0L;

        for (UserAttendRecordVO record : records) {
            if (Objects.isNull(record)
                    || !StrUtil.equals(record.getStatus(), "早退")
                    || StrUtil.isBlank(record.getCheckinTime())
                    || StrUtil.isBlank(record.getRuleCheckinTime())) {
                continue;
            }

            LocalDateTime checkinTime = LocalDateTimeUtil.parse(record.getCheckinTime(), "yyyy-MM-dd HH:mm");
            LocalDateTime ruleCheckinTime = LocalDateTimeUtil.parse(record.getRuleCheckinTime(), "yyyy-MM-dd HH:mm");

            if (checkinTime.isBefore(ruleCheckinTime)) {
                totalMinutes += Duration.between(checkinTime, ruleCheckinTime).toMinutes();
            }
        }

        return totalMinutes;
    }

    /**
     * 格式化早退时长。
     * <p>
     * 返回单位：分钟。
     * example:
     * 5
     * 80
     * 0
     */
    private Integer formatEarlyDuration(long totalMinutes) {
        if (totalMinutes <= 0) {
            return 0;
        }

        return Math.toIntExact(totalMinutes);
    }

    /**
     * 按状态统计考勤记录数量。
     */
    private int countAttendRecordsByStatus(List<UserAttendRecordVO> records, String status) {
        if (CollUtil.isEmpty(records) || StrUtil.isBlank(status)) {
            return 0;
        }

        return (int) records.stream()
                .filter(Objects::nonNull)
                .filter(record -> StrUtil.equals(record.getStatus(), status))
                .count();
    }

    /**
     * 计算本月出差天数。
     * <p>
     * 说明：
     * 1. 出差按自然日统计，和 calculateDays(LocalDate, LocalDate) 保持一致。
     * 2. 如果出差记录跨月，只统计落在当前月份内的日期。
     * 3. 如果多条出差记录覆盖同一天，同一天只算 1 次，避免重复叠加。
     */
    private AttendDurationBO calculateMonthBizTripDays(List<EkpAttendBusinessBO> tripInfo,
                                                       LocalDate monthStart,
                                                       LocalDate monthEnd) {
        if (CollUtil.isEmpty(tripInfo) || Objects.isNull(monthStart) || Objects.isNull(monthEnd)) {
            return buildAttendDurationBO(BigDecimal.ZERO, "0天");
        }

        Set<LocalDate> tripDays = new TreeSet<>();

        for (EkpAttendBusinessBO trip : tripInfo) {
            if (Objects.isNull(trip)
                    || Objects.isNull(trip.getStartTime())
                    || Objects.isNull(trip.getEndTime())) {
                continue;
            }

            LocalDate begin = trip.getStartTime().toLocalDate();
            LocalDate end = trip.getEndTime().toLocalDate();

            if (begin.isBefore(monthStart)) {
                begin = monthStart;
            }
            if (end.isAfter(monthEnd)) {
                end = monthEnd;
            }

            if (begin.isAfter(end)) {
                continue;
            }

            for (LocalDate day = begin; !day.isAfter(end); day = day.plusDays(1)) {
                tripDays.add(day);
            }
        }

        if (tripDays.isEmpty()) {
            return buildAttendDurationBO(BigDecimal.ZERO, "0天");
        }

        LocalDate firstDay = tripDays.iterator().next();
        LocalDate lastDay = null;
        for (LocalDate day : tripDays) {
            lastDay = day;
        }

        // 如果没有重复或断开的情况，直接复用 calculateDays。
        // 发生多条记录重复/断开时，以去重后的自然日数量为准。
        AttendDurationBO durationBO = calculateDays(firstDay, lastDay);
        if (durationBO.getDuration().intValue() == tripDays.size()) {
            return durationBO;
        }

        long days = tripDays.size();
        return buildAttendDurationBO(BigDecimal.valueOf(days), days + "天");
    }

    /**
     * 格式化月出差天数。
     */
    private String formatBizTripDaysText(AttendDurationBO durationBO) {
        if (Objects.isNull(durationBO)
                || Objects.isNull(durationBO.getDuration())
                || durationBO.getDuration().compareTo(BigDecimal.ZERO) <= 0) {
            return "-";
        }

        return StrUtil.blankToDefault(durationBO.getDurationFormat(), durationBO.getDuration().stripTrailingZeros().toPlainString() + "天");
    }

    /**
     * 计算本月加班小时数。
     * <p>
     * 说明：
     * 1. 加班时长复用 OvertimeDurationCalculator，保持和加班申请处一致。
     * 2. 返回单位是小时，例如：7、7.5。
     * 3. 如果加班记录跨月，会按本月查询区间裁剪，避免多算。
     */
    private BigDecimal calculateMonthOvertimeDuration(String weComId,
                                                      List<EkpAttendBusinessBO> overtimeInfo,
                                                      LocalDateTime monthBegin,
                                                      LocalDateTime monthEndExclusive,
                                                      LocalDate monthStart,
                                                      Map<Integer, AttendRuleBO> dayRuleMap) {
        if (StrUtil.isBlank(weComId) || CollUtil.isEmpty(overtimeInfo)) {
            return BigDecimal.ZERO;
        }

        OvertimeDurationCalculator calculator =
                new OvertimeDurationCalculator((itemWeComId, date) -> getMonthRuleOrLoad(itemWeComId, date, monthStart, dayRuleMap));

        BigDecimal total = BigDecimal.ZERO;

        for (EkpAttendBusinessBO overtime : overtimeInfo) {
            if (Objects.isNull(overtime)
                    || Objects.isNull(overtime.getStartTime())
                    || Objects.isNull(overtime.getEndTime())) {
                continue;
            }

            LocalDateTime begin = overtime.getStartTime().isBefore(monthBegin)
                    ? monthBegin
                    : overtime.getStartTime();

            LocalDateTime end = !overtime.getEndTime().isBefore(monthEndExclusive)
                    ? monthEndExclusive
                    : overtime.getEndTime();

            if (!end.isAfter(begin)) {
                continue;
            }

            AttendDurationBO durationBO = calculator.calculateDurationOfOvertime(
                    weComId,
                    begin,
                    end,
                    noNeedCheckinDates
            );

            if (Objects.nonNull(durationBO) && Objects.nonNull(durationBO.getDuration())) {
                total = total.add(durationBO.getDuration());
            }
        }

        return total.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros();
    }

    /**
     * 格式化加班小时数。
     */
    private String formatOvertimeDuration(BigDecimal duration) {
        return AttendDurationFormatter.formatHours(duration, "-");
    }

    /**
     * 格式化月考勤时长。
     */
    private String formatAttendDurationMore(long totalMinutes, long standardDayMinutes) {
        return AttendDurationFormatter.formatMinutesAsDaysHoursMinutes(totalMinutes, standardDayMinutes, "-");
    }

    /**
     * 计算当天请假分钟数。
     * <p>
     * 只统计请假落在当天工作时间段内的分钟数。
     * 例如普通班次 08:00-11:30、12:30-17:30：
     * 请假 11:00-13:00，只会统计 11:00-11:30 和 12:30-13:00。
     */
    private long calculateDayLeaveMinutes(List<EkpAttendBusinessBO> dayLeaveInfo,
                                          AttendRuleBO rule,
                                          LocalDate day) {
        if (CollUtil.isEmpty(dayLeaveInfo) || Objects.isNull(rule)) {
            return 0L;
        }

        long standardWorkMinutes = calculateStandardWorkMinutes(rule);
        if (standardWorkMinutes <= 0) {
            return 0L;
        }

        long leaveMinutes = calculateLeaveMinutesInWorkRanges(dayLeaveInfo, rule, day);
        if (leaveMinutes <= 0) {
            return 0L;
        }

        // 防止重复请假记录导致一天请假超过标准工作时长
        return Math.min(leaveMinutes, standardWorkMinutes);
    }

    /**
     * 根据请假分钟数折算请假天数。
     */
    private BigDecimal calculateDayLeaveDays(long dayLeaveMinutes, long standardWorkMinutes) {
        if (dayLeaveMinutes <= 0 || standardWorkMinutes <= 0) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(dayLeaveMinutes)
                .divide(BigDecimal.valueOf(standardWorkMinutes), 4, RoundingMode.HALF_UP);
    }

    /**
     * 计算当天实际出勤分钟数
     * 规则：
     * 1. 当天有至少一次真实打卡 / 外出 / 出差，基础出勤分钟 = 当天标准工作分钟
     * 2. 当天没有真实打卡、外出、出差，出勤分钟 = 0
     * 3. 有请假时，扣减请假落在工作时段内的分钟数
     * 4. 最低不小于0
     */
    private long calculateDayAttendMinutes(List<UserAttendRecordVO> actualRecords,
                                           List<EkpAttendBusinessBO> dayLeaveInfo,
                                           List<EkpAttendBusinessBO> dayOutInfo,
                                           List<EkpAttendBusinessBO> dayTripInfo,
                                           AttendRuleBO rule,
                                           LocalDate day) {
        boolean attended = hasAttendEvidence(actualRecords, dayOutInfo, dayTripInfo);

        if (!attended) {
            return 0L;
        }

        long standardWorkMinutes = calculateStandardWorkMinutes(rule);

        if (standardWorkMinutes <= 0) {
            return 0L;
        }

        long leaveMinutes = calculateLeaveMinutesInWorkRanges(dayLeaveInfo, rule, day);

        if (leaveMinutes <= 0) {
            return standardWorkMinutes;
        }

        leaveMinutes = Math.min(leaveMinutes, standardWorkMinutes);

        return Math.max(standardWorkMinutes - leaveMinutes, 0L);
    }


    /**
     * 计算当天出勤天数
     * <p>
     * 规则：
     * 1. 当天有至少一次真实打卡 / 外出 / 出差，基础出勤 = 1天
     * 2. 当天没有真实打卡、外出、出差，出勤 = 0天
     * 3. 有请假时，按 请假时长 / 当天应出勤时长 折算扣减
     * 4. 最低不小于0
     */
    private BigDecimal calculateDayAttendDays(List<UserAttendRecordVO> actualRecords,
                                              List<EkpAttendBusinessBO> dayLeaveInfo,
                                              List<EkpAttendBusinessBO> dayOutInfo,
                                              List<EkpAttendBusinessBO> dayTripInfo,
                                              AttendRuleBO rule,
                                              LocalDate day) {
        boolean attended = hasAttendEvidence(actualRecords, dayOutInfo, dayTripInfo);

        // 没有真实打卡、外出、出差，不算出勤
        if (!attended) {
            return BigDecimal.ZERO;
        }

        // 当天标准出勤分钟数
        long standardWorkMinutes = calculateStandardWorkMinutes(rule);

        if (standardWorkMinutes <= 0) {
            return BigDecimal.ONE;
        }

        // 请假落在当天工作时段内的分钟数
        long leaveMinutes = calculateLeaveMinutesInWorkRanges(dayLeaveInfo, rule, day);

        if (leaveMinutes <= 0) {
            return BigDecimal.ONE;
        }

        // 防止重复请假记录导致扣减超过 1 天
        leaveMinutes = Math.min(leaveMinutes, standardWorkMinutes);

        BigDecimal leaveDays = BigDecimal.valueOf(leaveMinutes)
                .divide(BigDecimal.valueOf(standardWorkMinutes), 4, RoundingMode.HALF_UP);

        BigDecimal result = BigDecimal.ONE.subtract(leaveDays);

        if (result.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }

        return result;
    }

    /**
     * 判断当天是否有出勤依据
     * <p>
     * 出勤依据包括：
     * 1. 至少一次真实打卡
     * 2. 当天存在外出记录
     * 3. 当天存在出差记录
     */
    private boolean hasAttendEvidence(List<UserAttendRecordVO> actualRecords,
                                      List<EkpAttendBusinessBO> dayOutInfo,
                                      List<EkpAttendBusinessBO> dayTripInfo) {
        boolean hasActualPunch = CollUtil.isNotEmpty(actualRecords)
                && actualRecords.stream()
                .anyMatch(item -> StrUtil.isNotBlank(item.getCheckinTime()));

        boolean hasOutgoing = CollUtil.isNotEmpty(dayOutInfo);
        boolean hasBusinessTrip = CollUtil.isNotEmpty(dayTripInfo);

        return hasActualPunch || hasOutgoing || hasBusinessTrip;
    }

    /**
     * 计算当天标准工作分钟数。
     */
    private long calculateStandardWorkMinutes(AttendRuleBO rule) {
        return AttendRuleWindowResolver.standardWorkMinutes(rule);
    }

    /**
     * 计算请假在当天工作时间段内占用的分钟数。
     */
    private long calculateLeaveMinutesInWorkRanges(List<EkpAttendBusinessBO> dayLeaveInfo,
                                                   AttendRuleBO rule,
                                                   LocalDate day) {
        return AttendTimeWindowUtil.calculateBizMinutesInWorkRanges(
                dayLeaveInfo,
                AttendRuleWindowResolver.resolveWorkCalcRanges(rule),
                day
        );
    }

    /**
     * 构建月考勤业务记录文案：请假 / 外出 / 出差 / 加班。
     */
    private List<String> buildMonthBizTexts(String bizName, List<EkpAttendBusinessBO> records) {
        return AttendBizTextFormatter.formatBizTexts(bizName, records, "MM-dd HH:mm");
    }

    /**
     * 构建月考勤打卡记录文案
     * <p>
     * example:
     * 07:30 正常
     * 11:34 正常
     * 12:30 上班缺卡
     */
    private String buildMonthAttendRecordText(UserAttendRecordVO record) {
        if (Objects.isNull(record)) {
            return "";
        }

        String timeText = "";

        if (StrUtil.isNotBlank(record.getCheckinTime())) {
            timeText = LocalDateTimeUtil.parse(record.getCheckinTime(), "yyyy-MM-dd HH:mm")
                    .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
        } else if (StrUtil.isNotBlank(record.getRuleCheckinTime())) {
            timeText = LocalDateTimeUtil.parse(record.getRuleCheckinTime(), "yyyy-MM-dd HH:mm")
                    .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
        }

        String status = StrUtil.blankToDefault(record.getStatus(), "正常");

        if (StrUtil.isBlank(timeText)) {
            return status;
        }

        return StrUtil.format("{} {}", timeText, status);
    }

    @Override
    public AttendDurationVO getBizTripDurationByEkpUserId(AttendBizTripDurationQuery query) {
        AttendDurationBO durationBO = this.calculateDays(query.getBeginTime(), query.getEndTime());
        return new AttendDurationVO(durationBO.getDuration(), durationBO.getDurationFormat());
    }

    @Override
    public AttendDurationVO getCurrentUserBizTripDuration(AttendBizTripDurationQuery query) {
        CmtLoginUser loginUser = (CmtLoginUser) authContext.getLoginUser();
        query.setUserEkpId(loginUser.getEkpId());
        return this.getBizTripDurationByEkpUserId(query);
    }

    @Override
    public void exportUserMonthAttend(AttendMonthDataQuery query) {
        // 创建导出记录
        String path = FesodExcelUtil.generateRandomXlsxFilePath();
        FileExportBO fileExportBO = new FileExportBO();
        fileExportBO.setPath(path);
        fileExportBO.setName(FileUtil.getName(path));
        fileExportBO.setOwnerType(AttachmentOwnerType.CMT_ATTEND_DATA.getCode());
        String exportId = fileExportCommonApi.save(fileExportBO);
        // 异步执行导出
        attendExportService.asyncExportMonthAttend(exportId, UploadUtil.UPLOAD_DIR + path, query);

    }

    @Override
    public List<FileExportVO> getAttendDataExportList() {
        return fileExportCommonApi.getExportList(
                AttachmentOwnerType.CMT_ATTEND_DATA.getCode(),
                authContext.getLoginUser().getId()
        );
    }

    private AttendDurationBO calculateDays(LocalDate beginTime, LocalDate endTime) {
        AttendDurationBO durationBO = new AttendDurationBO();
        if (beginTime == null || endTime == null || beginTime.isAfter(endTime)) {
            durationBO.setDuration(BigDecimal.ZERO);
            durationBO.setDurationFormat("0天");
            return durationBO;
        }

        long days = ChronoUnit.DAYS.between(beginTime, endTime) + 1;
        durationBO.setDuration(BigDecimal.valueOf(days));
        durationBO.setDurationFormat(days + "天");
        return durationBO;
    }

    @Override
    public AttendDurationBO calculateDurationOfAttend(String weComId, LocalDateTime beginTime, LocalDateTime endTime) {
        if (StrUtil.isBlank(weComId) || Objects.isNull(beginTime) || Objects.isNull(endTime) || !endTime.isAfter(beginTime)) {
            return buildAttendDurationBO(BigDecimal.ZERO, "0小时");
        }

        long totalMinutes = 0L;
        long formatStandardMinutesPerDay = 0L;
        LocalDate beginDate = beginTime.toLocalDate();
        LocalDate endDate = endTime.toLocalDate();

        for (LocalDate currentDate = beginDate; !currentDate.isAfter(endDate); currentDate = currentDate.plusDays(1)) {
            if (noNeedCheckinDates.contains(currentDate)) {
                continue;
            }

            AttendRuleBO rule = attendRuleService.getUserAttendRule(weComId, currentDate);
            if (Objects.isNull(rule) || AttendRuleType.EMPTY.equals(rule.getRuleType())) {
                continue;
            }

            int weekDay = currentDate.getDayOfWeek().getValue();
            if (rule.getWorkDays() == null || !ArrayUtil.contains(rule.getWorkDays(), weekDay)) {
                continue;
            }

            String[][] attendCalcRanges = getLeaveCalcRanges(rule);
            long dayMinutes = calculateAttendMinutesInRanges(currentDate, beginTime, endTime, attendCalcRanges);
            if (dayMinutes <= 0) {
                continue;
            }

            totalMinutes += dayMinutes;

            long standardMinutesPerDay = calculateRuleMinutes(attendCalcRanges);
            if (formatStandardMinutesPerDay <= 0 && standardMinutesPerDay > 0) {
                formatStandardMinutesPerDay = standardMinutesPerDay;
            }
        }

        BigDecimal duration = BigDecimal.valueOf(totalMinutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP)
                .stripTrailingZeros();

        String durationFormat = formatAttendDuration(totalMinutes, formatStandardMinutesPerDay);
        return buildAttendDurationBO(duration, durationFormat);
    }

    private long calculateAttendMinutesInRanges(LocalDate currentDate,
                                                LocalDateTime beginTime,
                                                LocalDateTime endTime,
                                                String[][] attendCalcRanges) {
        if (attendCalcRanges == null || attendCalcRanges.length == 0) {
            return 0L;
        }

        long totalMinutes = 0L;
        for (String[] range : attendCalcRanges) {
            if (range == null || range.length < 2) {
                continue;
            }

            LocalDateTime workStart = LocalDateTime.of(currentDate, LocalTime.parse(range[0]));
            LocalDateTime workEnd = LocalDateTime.of(currentDate, LocalTime.parse(range[1]));

            LocalDateTime actualStart = beginTime.isAfter(workStart) ? beginTime : workStart;
            LocalDateTime actualEnd = endTime.isBefore(workEnd) ? endTime : workEnd;

            if (actualEnd.isAfter(actualStart)) {
                totalMinutes += Duration.between(actualStart, actualEnd).toMinutes();
            }
        }
        return totalMinutes;
    }

    /**
     * 计算某天考勤规则的总工时（分钟）。
     */
    private long calculateRuleMinutes(String[][] ranges) {
        return AttendRuleWindowResolver.calculateRuleMinutes(ranges);
    }

    /**
     * 按规则动态格式化时长
     * <p>
     * 规则：
     * 1. “1天”不是固定8.5小时，而是按当天考勤规则总工时算
     * 2. 跨多天时，按每天规则工时依次折算
     * 3. 只有“超过一天”才显示“天”
     */
    private String formatAttendDuration(long totalMinutes, long standardMinutesPerDay) {
        if (totalMinutes <= 0) {
            return "0小时";
        }

        if (standardMinutesPerDay <= 0) {
            return formatHours(totalMinutes);
        }

        // 只有超过一天才显示“天”
        if (totalMinutes < standardMinutesPerDay) {
            return formatHours(totalMinutes);
        }

        long days = totalMinutes / standardMinutesPerDay;
        long remainMinutes = totalMinutes % standardMinutesPerDay;

        // 刚好整天时，如果你仍然想显示“17小时”而不是“2天”，这里要按你的业务决定
        // 当前写法：超过一天就显示天
        StringBuilder sb = new StringBuilder();
        sb.append(days).append("天");
        if (remainMinutes > 0) {
            sb.append(formatHoursWithoutUnit(remainMinutes)).append("小时");
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

    /**
     * 获取“用于计算请假/外出/出勤时长”的班次区间。
     */
    private String[][] getLeaveCalcRanges(AttendRuleBO rule) {
        return AttendRuleWindowResolver.resolveWorkCalcRanges(rule);
    }

    private Map<String, List<EkpAttendBusinessBO>> groupBizRecordsByEkpId(List<EkpAttendBusinessBO> records) {
        if (CollUtil.isEmpty(records)) {
            return Map.of();
        }
        return records.stream()
                .filter(Objects::nonNull)
                .filter(item -> StrUtil.isNotBlank(item.getEkpId()))
                .collect(Collectors.groupingBy(EkpAttendBusinessBO::getEkpId));
    }

    private Map<String, List<CmtAttendReissue>> groupReissueRecordsByEkpId(List<CmtAttendReissue> records) {
        if (CollUtil.isEmpty(records)) {
            return Map.of();
        }
        return records.stream()
                .filter(Objects::nonNull)
                .filter(item -> StrUtil.isNotBlank(item.getEkpUserId()))
                .collect(Collectors.groupingBy(CmtAttendReissue::getEkpUserId));
    }

    private <T> List<T> getRecordsByKey(Map<String, List<T>> recordsMap, String key) {
        if (StrUtil.isBlank(key) || Objects.isNull(recordsMap)) {
            return List.of();
        }
        return recordsMap.getOrDefault(key, List.of());
    }

    private Map<Integer, AttendRuleBO> getUserRuleMap(Map<String, Map<Integer, AttendRuleBO>> userRuleMap, String weComId) {
        if (StrUtil.isBlank(weComId) || Objects.isNull(userRuleMap)) {
            return Map.of();
        }
        return userRuleMap.getOrDefault(weComId, Map.of());
    }

    private Map<LocalDate, List<EkpAttendBusinessBO>> groupBizRecordsByDay(List<EkpAttendBusinessBO> source,
                                                                           LocalDate monthStart,
                                                                           LocalDate monthEnd) {
        if (CollUtil.isEmpty(source)) {
            return Map.of();
        }

        Map<LocalDate, List<EkpAttendBusinessBO>> result = new HashMap<>();
        for (EkpAttendBusinessBO record : source) {
            if (Objects.isNull(record)
                    || Objects.isNull(record.getStartTime())
                    || Objects.isNull(record.getEndTime())) {
                continue;
            }

            LocalDate start = record.getStartTime().toLocalDate();
            LocalDate end = record.getEndTime().toLocalDate();
            if (start.isBefore(monthStart)) {
                start = monthStart;
            }
            if (end.isAfter(monthEnd)) {
                end = monthEnd;
            }
            if (end.isBefore(start)) {
                continue;
            }

            for (LocalDate day = start; !day.isAfter(end); day = day.plusDays(1)) {
                if (filterBizByDay(List.of(record), day).isEmpty()) {
                    continue;
                }
                result.computeIfAbsent(day, key -> new ArrayList<>()).add(record);
            }
        }
        return result;
    }

    private Map<LocalDate, List<CmtAttendReissue>> groupReissueRecordsByDay(List<CmtAttendReissue> source) {
        if (CollUtil.isEmpty(source)) {
            return Map.of();
        }
        return source.stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getRuleCheckinTime() != null)
                .collect(Collectors.groupingBy(item -> item.getRuleCheckinTime().toLocalDate()));
    }

    private AttendRuleBO getMonthRuleOrLoad(String weComId,
                                            LocalDate day,
                                            LocalDate cacheMonthStart,
                                            Map<Integer, AttendRuleBO> dayRuleMap) {
        if (StrUtil.isBlank(weComId) || Objects.isNull(day)) {
            return null;
        }
        boolean inCacheMonth = Objects.nonNull(cacheMonthStart)
                && day.getYear() == cacheMonthStart.getYear()
                && day.getMonthValue() == cacheMonthStart.getMonthValue();
        if (inCacheMonth && Objects.nonNull(dayRuleMap)) {
            AttendRuleBO rule = dayRuleMap.get(day.getDayOfMonth());
            if (Objects.nonNull(rule)) {
                return rule;
            }
        }

        AttendRuleBO rule = attendRuleService.getUserAttendRule(weComId, day);
        if (inCacheMonth && Objects.nonNull(rule) && Objects.nonNull(dayRuleMap)) {
            dayRuleMap.put(day.getDayOfMonth(), rule);
        }
        return rule;
    }


    private List<EkpAttendBusinessBO> filterBizByDay(List<EkpAttendBusinessBO> source, LocalDate day) {
        return AttendTimeWindowUtil.filterBizByDay(source, day);
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

    @DS(GlobalConstants.DataSource.EKP_SQLSERVER)
    public List<EkpAttendBusinessBO> getUsersAttendBizRecords(List<String> ekpIds, LocalDateTime begin, LocalDateTime end, Integer bizType) {
        if (CollUtil.isEmpty(ekpIds)) {
            return List.of();
        }
        List<EkpAttendBusinessBO> result = new ArrayList<>();
        for (List<String> subEkpIds : CollUtil.split(ekpIds, 500)) {
            result.addAll(cmtAttendMapper.selectUsersEkpAttendBusiness(subEkpIds, begin, end, bizType));
        }
        return result;
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
        if (GlobalConstants.INT_NO.equals(dto.getIsSpecialCase())) {
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
        String ekpReviewId = cmtEkpService.startAttendReissueReview(dto, cmtUser);

        attendReissue.setEkpReviewId(ekpReviewId);
        attendReissueService.save(attendReissue);
    }

    @Override
    public void reissueAttendApplyForLoginUser(ReissueAttendDTO dto) {
        LoginUser loginUser = authContext.getLoginUserOrThrow();
        dto.setCmtUserId(loginUser.getId());
        SpringUtil.getBean(this.getClass()).reissueAttendApply(dto);
    }

    @Override
    @DSTransactional(rollbackFor = Exception.class)
    public void doReissueAttend(AttendReissueApplyPassDTO dto) {
        CmtAttendReissue attendReissue = attendReissueService.lambdaQuery().eq(CmtAttendReissue::getEkpReviewId, dto.getEkpReviewId()).one();
        if (Objects.isNull(attendReissue)) {
            throw new BizException("未找到对应的补卡申请记录");
        }
        if (GlobalConstants.INT_YES.equals(attendReissue.getIsApproved())) {
            return;
        }
        CmtUser cmtUser = cmtUserMapper.selectById(attendReissue.getCmtUserId());
        if (Objects.isNull(cmtUser)) {
            throw new BizException("未找到补卡申请的cmt用户，cmtUserId:" + attendReissue.getCmtUserId());
        }
        // 审批通过 添加企微补卡记录
        if (GlobalConstants.AttendReissueApprovalResult.APPROVED.equals(dto.getIsApproved())) {
            AttendRuleBO attendRule = attendRuleService.getUserAttendRule(cmtUser.getWeComId(), attendReissue.getRuleCheckinTime().toLocalDate());
            LocalDateTime dayBegin = LocalDateTimeUtil.beginOfDay(attendReissue.getRuleCheckinTime());
            LocalDateTime dayEnd = LocalDateTimeUtil.endOfDay(attendReissue.getRuleCheckinTime());
            List<UserAttendRecordVO> userAttendRecords = WeComApiUtil.getUserAttend(List.of(cmtUser.getWeComId()), dayBegin, dayEnd);
            LocalDateTime weComReissueTime = resolveWeComReissueTime(attendReissue, attendRule, userAttendRecords);
            if (!Objects.equals(weComReissueTime, attendReissue.getRuleCheckinTime())) {
                log.info("补卡企微写入时间已调整，用户:{}, 原规则时间:{}, 实际写入企微时间:{}",
                        cmtUser.getWeComId(), attendReissue.getRuleCheckinTime(), weComReissueTime);
            }
            WeComApiUtil.addUserAttend(cmtUser.getWeComId(), weComReissueTime);
        } else {
            // 审批被驳回 将蓝凌的审批流程删除
            cmtEkpService.deleteEkpReview(dto.getEkpReviewId());
        }
        attendReissueService.lambdaUpdate()
                .set(CmtAttendReissue::getIsApproved, dto.getIsApproved())
                .eq(CmtAttendReissue::getEkpReviewId, dto.getEkpReviewId())
                .update();
    }

    private LocalDateTime resolveWeComReissueTime(CmtAttendReissue attendReissue,
                                                  AttendRuleBO attendRule,
                                                  List<UserAttendRecordVO> userAttendRecords) {
        LocalDateTime ruleCheckinTime = attendReissue.getRuleCheckinTime();
        if (Objects.isNull(ruleCheckinTime)
                || Objects.isNull(attendRule)
                || CollUtil.isEmpty(userAttendRecords)) {
            return ruleCheckinTime;
        }

        LocalDateTime previousOffDutyTime = resolvePreviousOffDutyTime(attendRule, ruleCheckinTime);
        if (Objects.isNull(previousOffDutyTime)) {
            return ruleCheckinTime;
        }

        LocalDateTime weComDeadline = resolveWeComPreviousOffDutyDeadline(previousOffDutyTime, ruleCheckinTime);
        boolean previousOffDutyLatePunch = userAttendRecords.stream()
                .filter(item -> StrUtil.isNotBlank(item.getCheckinTime()))
                .map(item -> LocalDateTimeUtil.parse(item.getCheckinTime(), "yyyy-MM-dd HH:mm"))
                .anyMatch(checkinTime -> !checkinTime.isBefore(weComDeadline)
                        && checkinTime.isBefore(ruleCheckinTime));

        return previousOffDutyLatePunch ? previousOffDutyTime : ruleCheckinTime;
    }

    private LocalDateTime resolvePreviousOffDutyTime(AttendRuleBO attendRule, LocalDateTime ruleCheckinTime) {
        String[][] timeRanges = AttendRuleWindowResolver.resolveAttendRecordRanges(attendRule);
        if (timeRanges == null || timeRanges.length < 2) {
            return null;
        }

        LocalTime ruleTime = ruleCheckinTime.toLocalTime();
        for (int i = 1; i < timeRanges.length; i++) {
            String[] previousRange = timeRanges[i - 1];
            String[] currentRange = timeRanges[i];
            if (previousRange == null
                    || currentRange == null
                    || previousRange.length < 2
                    || currentRange.length < 1
                    || StrUtil.isBlank(previousRange[1])
                    || StrUtil.isBlank(currentRange[0])) {
                continue;
            }

            LocalTime previousOffDuty = LocalTime.parse(previousRange[1]);
            LocalTime currentOnDuty = LocalTime.parse(currentRange[0]);
            if (!currentOnDuty.equals(ruleTime) || !previousOffDuty.isBefore(currentOnDuty)) {
                continue;
            }
            return ruleCheckinTime.toLocalDate().atTime(previousOffDuty);
        }
        return null;
    }

    private LocalDateTime resolveWeComPreviousOffDutyDeadline(LocalDateTime previousOffDutyTime,
                                                             LocalDateTime currentOnDutyTime) {
        long halfBreakSeconds = Duration.between(previousOffDutyTime, currentOnDutyTime).getSeconds() / 2;
        return previousOffDutyTime.plusSeconds(halfBreakSeconds);
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


}
