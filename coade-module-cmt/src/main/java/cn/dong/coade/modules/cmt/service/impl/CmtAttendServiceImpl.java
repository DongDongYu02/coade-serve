package cn.dong.coade.modules.cmt.service.impl;

import cn.dong.coade.modules.cmt.domain.bo.EkpAttendBusinessBO;
import cn.dong.coade.modules.cmt.domain.bo.EkpAttendRuleBO;
import cn.dong.coade.modules.cmt.domain.dto.AttendReissueApplyPassDTO;
import cn.dong.coade.modules.cmt.domain.dto.ReissueAttendDTO;
import cn.dong.coade.modules.cmt.domain.entity.CmtAttendReissue;
import cn.dong.coade.modules.cmt.domain.entity.CmtUser;
import cn.dong.coade.modules.cmt.domain.vo.UserAttendInfoVO;
import cn.dong.coade.modules.cmt.domain.vo.UserAttendRecordVO;
import cn.dong.coade.modules.cmt.domain.vo.UserLeaveAttendVO;
import cn.dong.coade.modules.cmt.mapper.CmtAttendMapper;
import cn.dong.coade.modules.cmt.mapper.CmtUserMapper;
import cn.dong.coade.modules.cmt.service.ICmtAttendReissueService;
import cn.dong.coade.modules.cmt.service.ICmtAttendService;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CmtAttendServiceImpl implements ICmtAttendService {
    private final IAuthContext authContext;
    private final CmtAttendMapper cmtAttendMapper;
    private final CmtUserMapper cmtUserMapper;
    private final AttendRecordCalculator attendRecordCalculator;
    private final ICmtAttendReissueService attendReissueService;
    private final RestTemplate restTemplate;

    private static final String ATTEND_REISSUE_EKP_REVIEW_TEMPLATE_ID = "16be9d5fc79ef23244153e6457b9483a";

    private static final Map<String, EkpAttendRuleBO> ATTEND_RULE_MAP = Map.of(
            "多部门班次", new EkpAttendRuleBO(new String[][]{
                    {"08:00", "11:45"},
                    {"12:45", "17:30"}
            }, new int[]{1, 2, 3, 4, 5, 6}),
            "11:30两班次", new EkpAttendRuleBO(new String[][]{
                    {"08:00", "11:30"},
                    {"12:30", "17:30"}
            }, new int[]{1, 2, 3, 4, 5, 6}),
            "注塑部", new EkpAttendRuleBO(new String[][]{
                    {"08:00", "11:30"},
                    {"12:00", "17:30"},
                    {"18:00", "20:30"}
            }, new int[]{1, 2, 3, 4, 5, 6, 7}),
            "销售部", new EkpAttendRuleBO(new String[][]{
                    {"08:40", "11:30"},
                    {"12:30", "16:40"}
            }, new int[]{1, 2, 3, 4, 5}),
            "林克", new EkpAttendRuleBO(new String[][]{
                    {"08:30", "11:30"},
                    {"12:30", "17:00"}
            }, new int[]{2, 4, 6})
    );

    /**
     * 获取用户今日企微打卡记录
     */
    @Override
    @DS(GlobalConstants.DataSource.EKP_SQLSERVER)
    public UserAttendInfoVO getUserTodayAttend() {
        LoginUser loginUser = authContext.getLoginUserOrThrow();
        String weComId = loginUser.getExtInfo().get("weComId").toString();
        String ekpId = loginUser.getExtInfo().get("ekpId").toString();


        LocalDate now = LocalDate.now();
        LocalDateTime todayBegin = LocalDateTimeUtil.beginOfDay(now);
        LocalDateTime todayEnd = LocalDateTimeUtil.endOfDay(now);

        List<UserAttendRecordVO> userAttendToday = WeComApiUtil.getUserAttend(weComId, todayBegin, todayEnd);
        // 未关联蓝凌的用户
        if (GlobalConstants.UserIdentity.SPECIAL.equals(loginUser.getIdentity())) {
            userAttendToday.forEach(item -> item.setStatus("正常"));
            return new UserAttendInfoVO("暂无考勤规则", userAttendToday, new UserLeaveAttendVO());
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
            userAttendToday = userAttendToday.stream().filter(item -> {
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

        // 获取用户打卡规则
        String ruleGroupName = this.getUserAttendRule(ekpId);
        EkpAttendRuleBO rule = WeComApiUtil.getUserAttendRule(weComId, LocalDateTimeUtil.beginOfDay(LocalDateTime.now()));

        if (rule == null) {
            userAttendToday.forEach(item -> item.setStatus("正常"));
            return new UserAttendInfoVO("无需打卡", userAttendToday, new UserLeaveAttendVO());
        }
        String ruleInfo = this.buildRuleInfoText(rule);


        // 请假记录
        List<EkpAttendBusinessBO> leaveInfo = cmtAttendMapper.selectUserEkpAttendBusiness(ekpId, todayBegin, todayEnd, GlobalConstants.EkpLeaveType.LEAVE);
//        EkpAttendBusinessBO r = new EkpAttendBusinessBO();
//        r.setStartTime(LocalDateTime.of(2026, 3, 9, 17, 0));
//        r.setEndTime(LocalDateTime.of(2026, 3, 9, 17, 30));

//        List<EkpAttendBusinessBO> leaveInfo = List.of(r);
        // 外出记录
        List<EkpAttendBusinessBO> outInfo = cmtAttendMapper.selectUserEkpAttendBusiness(ekpId, todayBegin, todayEnd, GlobalConstants.EkpLeaveType.OUTGOING);
        // 出差记录
        List<EkpAttendBusinessBO> tripInfo = cmtAttendMapper.selectUserEkpAttendBusiness(ekpId, todayBegin, todayEnd, GlobalConstants.EkpLeaveType.BIZ_TRIP);

        UserLeaveAttendVO userLeaveAttendVO = attendRecordCalculator.buildUserTodayLeaveInfo(leaveInfo, outInfo, tripInfo);
        userAttendToday = attendRecordCalculator.calculate(
                now,
                userAttendToday,
                rule,
                leaveInfo,
                outInfo,
                tripInfo
        );

        // 将补卡记录的审批结果应用到打卡记录上
        if (!attendReissues.isEmpty()) {
            Map<LocalDateTime, Integer> reissueRecordsMap = attendReissues.stream().collect(Collectors.toMap(CmtAttendReissue::getRuleCheckinTime, CmtAttendReissue::getIsApproved));

            userAttendToday.forEach(record -> {
                LocalDateTime getRuleCheckinTime = LocalDateTimeUtil.parse(record.getRuleCheckinTime(), "yyyy-MM-dd HH:mm");
                if (reissueRecordsMap.containsKey(getRuleCheckinTime)) {
                    Integer isApproved = reissueRecordsMap.get(getRuleCheckinTime);
                    record.setExceptionStatus(isApproved);
                }
            });
        }
        return new UserAttendInfoVO(ruleInfo, userAttendToday, userLeaveAttendVO);
    }

    private String buildRuleInfoText(EkpAttendRuleBO rule) {
        String[][] timeRanges = rule.getTimeRanges();
        return Arrays.stream(timeRanges)
                .map(range -> StrUtil.format("{}-{}", range[0], range[1]))
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
