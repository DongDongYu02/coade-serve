package cn.dong.coade.modules.cmt.service;

import cn.dong.coade.modules.cmt.constants.CmtLocalConstants;
import cn.dong.coade.modules.cmt.domain.bo.CmtLoginUser;
import cn.dong.coade.modules.cmt.domain.bo.EkpAttachmentBO;
import cn.dong.coade.modules.cmt.domain.dto.*;
import cn.dong.coade.modules.cmt.domain.entity.CmtDepartment;
import cn.dong.coade.modules.cmt.domain.entity.CmtUser;
import cn.dong.coade.modules.cmt.mapper.CmtEkpMapper;
import cn.dong.nexus.common.constants.ApiConstants;
import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.core.api.ApiMessage;
import cn.dong.nexus.core.config.properties.CoadeProperties;
import cn.dong.nexus.core.exception.BizException;
import cn.dong.nexus.core.security.context.IAuthContext;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 查 EKP 数据服务类
 */
@Service
@RequiredArgsConstructor
@DS(GlobalConstants.DataSource.EKP_SQLSERVER)
@Slf4j
public class CmtEkpService {
    private final CmtEkpMapper cmtEkpMapper;
    private final CoadeProperties coadeProperties;
    private final IAuthContext authContext;
    private final RestTemplate restTemplate;

    /**
     * 查询 EKP 的部门
     */
    @DS(GlobalConstants.DataSource.EKP_SQLSERVER)
    public List<CmtDepartment> getEkpDepartments() {
        return cmtEkpMapper.selectEkpDepartments();
    }


    @DS(GlobalConstants.DataSource.EKP_SQLSERVER)
    public Map<String, List<String>> getAttachmentsKeyMap(List<String> keys) {
        List<EkpAttachmentBO> attachments = cmtEkpMapper.selectAttachmentsByKeys(keys);
        if (attachments.isEmpty()) {
            return Map.of();
        }

        return attachments.stream()
                // 组装文件名
                .peek(item -> item.setAttPath(item.getAttKey() + FileUtil.extName(item.getAttName())))
                // 根据attKey分组
                .collect(Collectors.groupingBy(
                        EkpAttachmentBO::getAttKey,
                        Collectors.mapping(EkpAttachmentBO::getAttPath, Collectors.toList())));
    }


    @DS(GlobalConstants.DataSource.EKP_SQLSERVER)
    public Map<String, String> getAttachmentKeyMap(List<String> keys) {
        List<EkpAttachmentBO> attachments = cmtEkpMapper.selectAttachmentsByKeys(keys);
        if (attachments.isEmpty()) {
            return Map.of();
        }

        return attachments.stream()
                // 组装文件名
                .peek(item -> item.setAttPath(item.getAttKey() + "." + FileUtil.extName(item.getAttName())))
                // 根据attKey分组
                .collect(Collectors.toMap(EkpAttachmentBO::getAttKey, EkpAttachmentBO::getAttPath));
    }

    @DS(GlobalConstants.DataSource.EKP_SQLSERVER)
    public List<EkpAttachmentBO> getAttachmentsByKeys(List<String> keys) {
        List<EkpAttachmentBO> ekpAttachments = cmtEkpMapper.selectAttachmentsByKeys(keys);
        if (ekpAttachments.isEmpty()) {
            return List.of();
        }
        ekpAttachments.forEach(item -> item.setAttExtName(FileUtil.extName(item.getAttName())));
        return ekpAttachments;
    }


    public File getAttachmentFile(String attId, File file) {
        String url = UrlBuilder
                .of(coadeProperties.getEkp().getServerUrl() + ApiConstants.EKP_DOWNLOAD_FILE)
                .addQuery("fdId", attId)
                .build();
        String downloadUrl = HttpUtil.get(url);
        return HttpUtil.downloadFileFromUrl(downloadUrl, file);
    }

    /**
     * 启动请假申请流程
     *
     */
    public String startLeaveRequestReview(AttendLeaveRequestDTO dto, CmtLoginUser loginUser) {
        String url = coadeProperties.getEkp().getServerUrl() + ApiConstants.INITIATE_EKP_REVIEW;
        String templateId = coadeProperties.getEkp().getReview().getLeaveRequestReviewTemplateId();
        String typeText = CmtLocalConstants.LEAVE_REQUEST_TYPE.DICT_MAP.getOrDefault(dto.getType(), CmtLocalConstants.LEAVE_REQUEST_TYPE.PERSONAL_TEXT);
        String docSubject = StrUtil.format("{}提交的{}申请", loginUser.getUsername(), typeText);
        String docCreator = buildUserFieldByEkpId(loginUser.getEkpId());
        JSONObject content = new JSONObject();
        CoadeProperties.Ekp.Review.LeaveRequestField leaveRequestField = coadeProperties.getEkp().getReview().getLeaveRequestField();
        // 请假类型
        content.set(leaveRequestField.getType(), dto.getType());
        content.set(leaveRequestField.getTypeText(), typeText);
        // 请假开始日期
        content.set(leaveRequestField.getBeginTime(), dto.getBeginTime().format(GlobalConstants.DateFormat.Y_M_D_H_M));
        // 请假结束日期
        content.set(leaveRequestField.getEndTime(), dto.getEndTime().format(GlobalConstants.DateFormat.Y_M_D_H_M));
        // 请假时长
        content.set(leaveRequestField.getDuration(), dto.getDuration().doubleValue());
        // 请假时长显示值
        content.set(leaveRequestField.getDurationFormat(), dto.getDurationFormat());
        // 请假原因
        content.set(leaveRequestField.getReason(), dto.getReason());

        MultiValueMap<String, Object> wholeForm = new LinkedMultiValueMap<>();
        wholeForm.add("docSubject", docSubject);
        wholeForm.add("docCreator", docCreator);
        wholeForm.add("docStatus", 20);
        wholeForm.add("fdTemplateId", templateId);
        wholeForm.add("formValues", content.toJSONString(1));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(wholeForm, headers);

        String body;
        try {
            ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            body = exchange.getBody();
        } catch (RestClientException e) {
            log.error("发起EKP请假流程失败:{}", e.getMessage());
            throw new BizException(ApiMessage.INTERNAL_ERROR);
        }
        if (JSONUtil.isTypeJSON(body)) {
            log.error("发起EKP请假流程失败:{}", body);
            throw new BizException(ApiMessage.INTERNAL_ERROR);
        }
        return body;
    }

    private String buildUserFieldByEkpId(String ekpId) {
        if (StrUtil.isBlank(ekpId)) {
            return "";
        }
        return StrUtil.format("""
                {"Id":"{}"}
                """, ekpId);
    }

    /**
     * 删除蓝凌审批流
     *
     * @param ekpReviewId ekp 审批流ID
     */
    @DSTransactional(rollbackFor = Exception.class)
    public void deleteEkpReview(String ekpReviewId) {
        cmtEkpMapper.deleteReviewAreader(ekpReviewId);
        cmtEkpMapper.deleteReviewOreader(ekpReviewId);
        cmtEkpMapper.deleteReviewAeditor(ekpReviewId);
        cmtEkpMapper.deleteReviewOeditor(ekpReviewId);
        cmtEkpMapper.deleteBookingReview(ekpReviewId);
        cmtEkpMapper.deleteReviewTodo(ekpReviewId);
    }

    /**
     * 启动外出申请审批流
     */
    public String startOutgoingRequestReview(AttendOutgoingRequestDTO dto, CmtLoginUser loginUser) {
        String url = coadeProperties.getEkp().getServerUrl() + ApiConstants.INITIATE_EKP_REVIEW;
        String templateId = coadeProperties.getEkp().getReview().getOutgoingRequestReviewTemplateId();
        String docCreator = buildUserFieldByEkpId(loginUser.getEkpId());
        JSONObject content = new JSONObject();
        CoadeProperties.Ekp.Review.OutgoingRequestField outgoingRequestField = coadeProperties.getEkp().getReview().getOutgoingRequestField();
        // 外出日期
        content.set(outgoingRequestField.getOutDate(), dto.getOutDate().format(GlobalConstants.DateFormat.NORMAL_ONLY_DATE));
        // 外出开始时间
        content.set(outgoingRequestField.getOutTimeBegin(), dto.getOutTimeBegin().format(GlobalConstants.DateFormat.TIME));
        // 外出结束时间
        content.set(outgoingRequestField.getOutTimeEnd(), dto.getOutTimeEnd().format(GlobalConstants.DateFormat.TIME));
        // 外出时长
        content.set(outgoingRequestField.getDuration(), dto.getDuration().doubleValue());
        // 外出事由
        content.set(outgoingRequestField.getReason(), dto.getReason());
        MultiValueMap<String, Object> wholeForm = new LinkedMultiValueMap<>();
        wholeForm.add("docCreator", docCreator);
        wholeForm.add("docStatus", 20);
        wholeForm.add("fdTemplateId", templateId);
        wholeForm.add("formValues", content.toJSONString(1));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(wholeForm, headers);

        String body;
        try {
            ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            body = exchange.getBody();
        } catch (RestClientException e) {
            log.error("发起EKP外出流程失败:{}", e.getMessage());
            throw new BizException(ApiMessage.INTERNAL_ERROR);
        }
        if (JSONUtil.isTypeJSON(body)) {
            log.error("发起EKP外出流程失败:{}", body);
            throw new BizException(ApiMessage.INTERNAL_ERROR);
        }
        return body;
    }

    /**
     * 启动出差申请审批流
     */
    public String startBizTripRequestReview(AttendBizTripRequestDTO dto, CmtLoginUser loginUser) {
        String url = coadeProperties.getEkp().getServerUrl() + ApiConstants.INITIATE_EKP_REVIEW;
        String templateId = coadeProperties.getEkp().getReview().getBizTripRequestReviewTemplateId();
        String docCreator = buildUserFieldByEkpId(loginUser.getEkpId());
        JSONObject content = new JSONObject();
        CoadeProperties.Ekp.Review.BizTripRequestField bizTripRequestField = coadeProperties.getEkp().getReview().getBizTripRequestField();
        // 出差开始时间
        content.set(bizTripRequestField.getBeginTime(), dto.getBeginTime().format(GlobalConstants.DateFormat.Y_M_D_H_M));
        // 出差结束时间
        content.set(bizTripRequestField.getEndTime(), dto.getEndTime().format(GlobalConstants.DateFormat.Y_M_D_H_M));
        // 出差时长
        content.set(bizTripRequestField.getDuration(), dto.getDuration().doubleValue());
        // 出差时长显示值
        content.set(bizTripRequestField.getDurationFormat(), dto.getDurationFormat());
        // 出差事由
        content.set(bizTripRequestField.getReason(), dto.getReason());
        MultiValueMap<String, Object> wholeForm = new LinkedMultiValueMap<>();
        wholeForm.add("docCreator", docCreator);
        wholeForm.add("docStatus", 20);
        wholeForm.add("fdTemplateId", templateId);
        wholeForm.add("formValues", content.toJSONString(1));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(wholeForm, headers);

        String body;
        try {
            ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            body = exchange.getBody();
        } catch (RestClientException e) {
            log.error("发起EKP出差流程失败:{}", e.getMessage());
            throw new BizException(ApiMessage.INTERNAL_ERROR);
        }
        if (JSONUtil.isTypeJSON(body)) {
            log.error("发起EKP出差流程失败:{}", body);
            throw new BizException(ApiMessage.INTERNAL_ERROR);
        }
        return body;
    }

    /**
     * 发起加班审批流
     */
    public String startOvertimeRequestReview(AttendOvertimeRequestDTO dto, CmtLoginUser loginUser) {
        String url = coadeProperties.getEkp().getServerUrl() + ApiConstants.INITIATE_EKP_REVIEW;
        String templateId = coadeProperties.getEkp().getReview().getOvertimeRequestReviewTemplateId();
        String docSubject = StrUtil.format("{}提交的加班申请", loginUser.getUsername());
        String docCreator = buildUserFieldByEkpId(loginUser.getEkpId());
        JSONObject content = new JSONObject();
        CoadeProperties.Ekp.Review.OvertimeRequestField overtimeRequestField = coadeProperties.getEkp().getReview().getOvertimeRequestField();
        // 加班日期
        content.set(overtimeRequestField.getOvertimeDate(), dto.getOvertimeDate().format(GlobalConstants.DateFormat.NORMAL_ONLY_DATE));
        // 加班开始时间
        content.set(overtimeRequestField.getBeginTime(), dto.getBeginTime().format(GlobalConstants.DateFormat.TIME));
        // 加班结束时间
        content.set(overtimeRequestField.getEndTime(), dto.getEndTime().format(GlobalConstants.DateFormat.TIME));
        // 加班时长
        content.set(overtimeRequestField.getDuration(), dto.getDuration().doubleValue());
        // 加班事由
        content.set(overtimeRequestField.getReason(), dto.getReason());

        MultiValueMap<String, Object> wholeForm = new LinkedMultiValueMap<>();
        wholeForm.add("docSubject", docSubject);
        wholeForm.add("docCreator", docCreator);
        wholeForm.add("docStatus", 20);
        wholeForm.add("fdTemplateId", templateId);
        wholeForm.add("formValues", content.toJSONString(1));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(wholeForm, headers);

        String body;
        try {
            ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            body = exchange.getBody();
        } catch (RestClientException e) {
            log.error("发起EKP加班流程失败:{}", e.getMessage());
            throw new BizException(ApiMessage.INTERNAL_ERROR);
        }
        if (JSONUtil.isTypeJSON(body)) {
            log.error("发起EKP加班流程失败:{}", body);
            throw new BizException(ApiMessage.INTERNAL_ERROR);
        }
        return body;
    }

    public String startAttendReissueReview(ReissueAttendDTO dto, CmtUser cmtUser) {
        String url = coadeProperties.getEkp().getServerUrl() + ApiConstants.INITIATE_EKP_REVIEW;
        boolean isSpecialCase = GlobalConstants.INT_YES.equals(dto.getIsSpecialCase());
        String templateId = isSpecialCase ?
                coadeProperties.getEkp().getReview().getAttendSpecialCaseReissueReviewTemplateId() :
                coadeProperties.getEkp().getReview().getAttendReissueReviewTemplateId();
        String docCreator = buildUserFieldByEkpId(cmtUser.getEkpId());
        String docSubject = StrUtil.format("{}的打卡异常处理申请", cmtUser.getUsername());
        JSONObject content = new JSONObject();
        CoadeProperties.Ekp.Review.AttendReissueField attendReissueField = coadeProperties.getEkp().getReview().getAttendReissueField();
        CoadeProperties.Ekp.Review.AttendSpecialCaseReissueField attendSpecialCaseReissueField = coadeProperties.getEkp().getReview().getAttendSpecialCaseReissueField();
        // 打卡时间
        content.set(isSpecialCase ? attendSpecialCaseReissueField.getCheckinTime() : attendReissueField.getCheckinTime(), dto.getCheckinTime());
        // 规则打卡时间
        content.set(isSpecialCase ? attendSpecialCaseReissueField.getRuleCheckinTime() : attendReissueField.getRuleCheckinTime(), dto.getRuleCheckinTime());
        // 异常原因
        content.set(isSpecialCase ? attendSpecialCaseReissueField.getReason() : attendReissueField.getReason(), dto.getReason());
        // 异常状态
        content.set(isSpecialCase ? attendSpecialCaseReissueField.getReissueType() : attendReissueField.getReissueType(), dto.getReissueType());

        MultiValueMap<String, Object> wholeForm = new LinkedMultiValueMap<>();
        wholeForm.add("docSubject", docSubject);
        wholeForm.add("docCreator", docCreator);
        wholeForm.add("docStatus", 20);
        wholeForm.add("fdTemplateId", templateId);
        wholeForm.add("formValues", content.toJSONString(1));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(wholeForm, headers);

        String body;
        try {
            ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            body = exchange.getBody();
        } catch (RestClientException e) {
            log.error("发起EKP补卡流程失败:{}", e.getMessage());
            throw new BizException(ApiMessage.INTERNAL_ERROR);
        }
        if (JSONUtil.isTypeJSON(body)) {
            log.error("发起EKP补卡流程失败:{}", body);
            throw new BizException(ApiMessage.INTERNAL_ERROR);
        }
        return body;

    }
}
