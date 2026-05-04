package cn.dong.coade.modules.cmt.service.impl;

import cn.dong.coade.modules.cmt.constants.CmtLocalConstants;
import cn.dong.coade.modules.cmt.domain.bo.WeComCardMessageBO;
import cn.dong.coade.modules.cmt.domain.dto.*;
import cn.dong.coade.modules.cmt.domain.entity.CmtIssueDemand;
import cn.dong.coade.modules.cmt.domain.entity.CmtUser;
import cn.dong.coade.modules.cmt.domain.query.IssueDemandQuery;
import cn.dong.coade.modules.cmt.domain.vo.IssueDemandDetailVO;
import cn.dong.coade.modules.cmt.domain.vo.IssueDemandVO;
import cn.dong.coade.modules.cmt.mapper.CmtIssueDemandMapper;
import cn.dong.coade.modules.cmt.service.ICmtIssueDemandService;
import cn.dong.coade.modules.cmt.service.ICmtUserService;
import cn.dong.coade.modules.cmt.service.IWeComService;
import cn.dong.nexus.common.api.AttachmentCommonApi;
import cn.dong.nexus.common.constants.AttachmentOwnerType;
import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.common.domain.bo.AttachmentBO;
import cn.dong.nexus.common.domain.vo.AttachmentVO;
import cn.dong.nexus.common.utils.CommonUtil;
import cn.dong.nexus.core.api.ApiMessage;
import cn.dong.nexus.core.base.SelectionVO;
import cn.dong.nexus.core.config.properties.CoadeProperties;
import cn.dong.nexus.core.exception.BizException;
import cn.dong.nexus.core.resmapping.ResMappingUtil;
import cn.dong.nexus.core.security.context.IAuthContext;
import cn.dong.nexus.core.util.PageUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CmtIssueDemandServiceImpl extends ServiceImpl<CmtIssueDemandMapper, CmtIssueDemand> implements ICmtIssueDemandService {

    private final AttachmentCommonApi attachmentService;
    private final CoadeProperties coadeProperties;
    private final ICmtUserService userService;
    private final IAuthContext authContext;
    private final IWeComService weComService;

    @Override
    @Transactional(rollbackFor = Exception.class)

    public void create(IssueDemandDTO dto) {
        CmtIssueDemand entity = dto.toEntity();
        this.save(entity);
        attachmentService.saveAttachmentsOwner(dto.getAttachmentIds(), AttachmentOwnerType.CMT_ISSUE_DEMAND, entity.getId());
    }

    @Override
    public IPage<IssueDemandVO> getPageList(IssueDemandQuery query) {
        Page<CmtIssueDemand> page = this.page(query.toPage(), query.toQueryWrapper());
        if (page.getRecords().isEmpty()) {
            return PageUtil.emptyPage();
        }
        return PageUtil.convertPage(page, IssueDemandVO.class);
    }

    @Override
    public List<IssueDemandVO> getList(IssueDemandQuery query) {
        QueryWrapper<CmtIssueDemand> queryWrapper = query.toQueryWrapper();
        // 查询最近三个月的记录
        queryWrapper.lambda()
                .gt(CmtIssueDemand::getCreateTime, LocalDateTime.now().minusMonths(3))
                .or(!GlobalConstants.UserIdentity.ADMIN.equals(authContext.getLoginUser().getIdentity()),
                        wrapper ->
                                wrapper.eq(CmtIssueDemand::getCreateBy, authContext.getLoginUser().getId())
                                        .or()
                                        .eq(CmtIssueDemand::getPrincipalUserId, authContext.getLoginUser().getId())
                );
        List<CmtIssueDemand> records = this.list(queryWrapper);
        if (records.isEmpty()) {
            return List.of();
        }
        List<IssueDemandVO> result = BeanUtil.copyToList(records, IssueDemandVO.class);
        result.forEach(item -> {
            String devCostTime = this.computedDevCostTime(item);
            Integer acceptanceIsOverdue = this.computedAcceptanceIsOverdue(item);
            Integer devIsOverdue = this.computedDevIsOverdue(item);
            item.setDevCostTime(devCostTime);
            item.setAcceptanceIsOverdue(acceptanceIsOverdue);
            item.setDevIsOverdue(devIsOverdue);
        });
        return result;
    }

    /**
     * 计算开发耗时
     */
    private String computedDevCostTime(IssueDemandVO vo) {
        if (CmtLocalConstants.ISSUE_DEMAND_STATUS.COMPLETED.equals(vo.getStatus()) ||
                CmtLocalConstants.ISSUE_DEMAND_STATUS.PENDING_ACCEPT.equals(vo.getStatus())) {
            // 开发耗时
            if (Objects.nonNull(vo.getDevStartTime()) && Objects.nonNull(vo.getActualFinishTime())) {
                Duration duration = LocalDateTimeUtil.between(vo.getDevStartTime(), vo.getActualFinishTime());
                return CommonUtil.formatDuration(duration);
            }
        }
        if (CmtLocalConstants.ISSUE_DEMAND_STATUS.VOIDED.equals(vo.getStatus())) {
            // 开发耗时
            if (Objects.nonNull(vo.getDevStartTime()) && Objects.nonNull(vo.getVoidedTime())) {
                Duration duration = LocalDateTimeUtil.between(vo.getDevStartTime(), vo.getVoidedTime());
                return CommonUtil.formatDuration(duration);
            }
        }
        return null;
    }

    /**
     * 计算总耗时
     */
    private String computedTotalCostTime(IssueDemandVO vo) {
        if (CmtLocalConstants.ISSUE_DEMAND_STATUS.COMPLETED.equals(vo.getStatus())) {
            if (Objects.nonNull(vo.getCreateTime()) && Objects.nonNull(vo.getActualFinishTime())) {
                Duration duration = LocalDateTimeUtil.between(vo.getCreateTime(), vo.getActualFinishTime());
                return CommonUtil.formatDuration(duration);
            }
        }
        if (CmtLocalConstants.ISSUE_DEMAND_STATUS.VOIDED.equals(vo.getStatus())) {
            if (Objects.nonNull(vo.getCreateTime()) && Objects.nonNull(vo.getVoidedTime())) {
                Duration duration = LocalDateTimeUtil.between(vo.getCreateTime(), vo.getVoidedTime());
                return CommonUtil.formatDuration(duration);
            }
        }
        if (CmtLocalConstants.ISSUE_DEMAND_STATUS.REJECTED.equals(vo.getStatus())) {
            if (Objects.nonNull(vo.getCreateTime()) && Objects.nonNull(vo.getRejectTime())) {
                Duration duration = LocalDateTimeUtil.between(vo.getCreateTime(), vo.getVoidedTime());
                return CommonUtil.formatDuration(duration);
            }
        }
        return null;
    }

    /**
     * 计算开发是否逾期
     *
     * @return 1：已逾期；0：未逾期；null：无法判断/不适用
     */
    private Integer computedDevIsOverdue(IssueDemandVO vo) {
        if (Objects.isNull(vo) || Objects.isNull(vo.getExpectedFinishTime())) {
            return null;
        }

        Integer status = vo.getStatus();
        LocalDateTime planFinishTime = vo.getPlanFinishTime();

        // 已完成 / 待验收：用实际完成时间判断
        if (CmtLocalConstants.ISSUE_DEMAND_STATUS.COMPLETED.equals(status)
                || CmtLocalConstants.ISSUE_DEMAND_STATUS.PENDING_ACCEPT.equals(status)) {

            if (Objects.isNull(vo.getActualFinishTime())) {
                return null;
            }

            return vo.getActualFinishTime().isAfter(planFinishTime) ? 1 : 0;
        }

        // 作废：用作废时间判断
        if (CmtLocalConstants.ISSUE_DEMAND_STATUS.VOIDED.equals(status)) {
            if (Objects.isNull(vo.getVoidedTime())) {
                return null;
            }

            return vo.getVoidedTime().isAfter(planFinishTime) ? 1 : 0;
        }

        // 开发中：用当前时间判断
        if (CmtLocalConstants.ISSUE_DEMAND_STATUS.IN_PROGRESS.equals(status)) {
            return LocalDateTime.now().isAfter(planFinishTime) ? 1 : 0;
        }

        return null;
    }

    /**
     * 计算验收是否逾期
     */
    private Integer computedAcceptanceIsOverdue(IssueDemandVO vo) {
        if (Objects.isNull(vo) || Objects.isNull(vo.getActualFinishTime())) {
            return null;
        }
        Integer status = vo.getStatus();
        // 实际完成时间，也就是进入待验收的时间
        LocalDateTime actualFinishTime = vo.getActualFinishTime();
        // 验收截止时间：完成开发后 24 小时内验收
        LocalDateTime acceptanceDeadlineTime = actualFinishTime.plusHours(24);
        // 已完成：用验收通过时间判断
        if (CmtLocalConstants.ISSUE_DEMAND_STATUS.COMPLETED.equals(status)) {
            LocalDateTime acceptanceTime = vo.getAcceptanceTime();
            if (Objects.isNull(acceptanceTime)) {
                return null;
            }
            return acceptanceTime.isAfter(acceptanceDeadlineTime) ? 1 : 0;
        }
        // 待验收：用当前时间判断
        if (CmtLocalConstants.ISSUE_DEMAND_STATUS.PENDING_ACCEPT.equals(status)) {
            return LocalDateTime.now().isAfter(acceptanceDeadlineTime) ? 1 : 0;
        }
        return null;
    }

    @Override
    public IssueDemandDetailVO getDetailById(String id) {
        CmtIssueDemand issueDemand = this.getById(id);
        if (Objects.isNull(issueDemand)) {
            throw new BizException(ApiMessage.NOT_FOUND);
        }
        IssueDemandDetailVO detail = BeanUtil.copyProperties(issueDemand, IssueDemandDetailVO.class);
        List<AttachmentBO> attachments = attachmentService.getByOwners(AttachmentOwnerType.CMT_ISSUE_DEMAND, List.of(detail.getId()));
        detail.setAttachments(BeanUtil.copyToList(attachments, AttachmentVO.class));
        IssueDemandVO temp = BeanUtil.copyProperties(issueDemand, IssueDemandVO.class);
        String devCostTime = this.computedDevCostTime(temp);
        Integer acceptanceIsOverdue = this.computedAcceptanceIsOverdue(temp);
        Integer devIsOverdue = this.computedDevIsOverdue(temp);
        String totalCostTime = this.computedTotalCostTime(temp);
        detail.setDevCostTime(devCostTime);
        detail.setAcceptanceIsOverdue(acceptanceIsOverdue);
        detail.setDevIsOverdue(devIsOverdue);
        detail.setTotalCostTime(totalCostTime);

        return detail;
    }

    @Override
    public void edit(IssueDemandDTO dto) {
        dto.doValidate();
    }

    @Override
    public void assigned(String id, String principalUserId) {
        CmtIssueDemand issueDemand = this.getById(id);
        if (Objects.isNull(issueDemand)) {
            throw new BizException(ApiMessage.NOT_FOUND);
        }
        String principalUserName = ResMappingUtil.getFieldMappingValue(principalUserId, CmtUser::getId, CmtUser::getUsername);
        this.lambdaUpdate().eq(CmtIssueDemand::getId, id)
                .set(CmtIssueDemand::getPrincipalUserId, principalUserId)
                .set(CmtIssueDemand::getPrincipalUserName, principalUserName)
                .set(CmtIssueDemand::getStatus, CmtLocalConstants.ISSUE_DEMAND_STATUS.ASSESSING)
                .update();
        WeComCardMessageBO message = new WeComCardMessageBO();
        String category = CmtLocalConstants.ISSUE_DEMAND_TYPE.DEMAND.equals(issueDemand.getType()) ? "需求开发" : "系统优化";
        String title = category + "指派通知";
        String description = StrUtil.format("""
                        <div class="gray">{}</div>
                        <div class="normal">有一条新的{}指派你为负责人，请查收</div>
                        <div class="highlight">期望完成日期：{}</div>
                        """,
                LocalDateTime.now().format(GlobalConstants.DateFormat.Y_M_D_H_M),
                category,
                Objects.nonNull(issueDemand.getExpectedFinishTime()) ? issueDemand.getExpectedFinishTime().format(GlobalConstants.DateFormat.NORMAL_ONLY_DATE) : "-");
        String url = coadeProperties.getCmt().getDomain() + "/issue-hub/" + issueDemand.getId();
        message.setTextcard(new WeComCardMessageBO.Content(title, description, url, "查看详情"));
        message.setAgentid(coadeProperties.getCmt().getWeComAgentId());
        weComService.sendMarkdownMessage(principalUserId, message);
    }

    @Override
    public void assessmented(IssueDemandAssessmentedDTO dto) {
        CmtIssueDemand issueDemand = this.getById(dto.getId());
        if (Objects.isNull(issueDemand)) {
            throw new BizException(ApiMessage.NOT_FOUND);
        }
        // 完成评估 -> 状态：开发中 , 记录计划完成时间、开发开始时间 , 若期望完成时间为空，则设置为评估时传入的计划完成时间
        this.lambdaUpdate().eq(CmtIssueDemand::getId, issueDemand.getId())
                .set(CmtIssueDemand::getStatus, CmtLocalConstants.ISSUE_DEMAND_STATUS.IN_PROGRESS)
                .set(Objects.isNull(issueDemand.getExpectedFinishTime()), CmtIssueDemand::getExpectedFinishTime, dto.getPlanFinishTime())
                .set(CmtIssueDemand::getDevStartTime, LocalDateTime.now())
                .set(CmtIssueDemand::getPlanFinishTime, dto.getPlanFinishTime())
                .update();
    }

    @Override
    public void reject(IssueDemandRejectDTO dto) {
        CmtIssueDemand issueDemand = this.getById(dto.getId());
        if (Objects.isNull(issueDemand)) {
            throw new BizException(ApiMessage.NOT_FOUND);
        }
        this.lambdaUpdate().eq(CmtIssueDemand::getId, issueDemand.getId())
                .set(CmtIssueDemand::getStatus, CmtLocalConstants.ISSUE_DEMAND_STATUS.REJECTED)
                .set(CmtIssueDemand::getRejectReason, dto.getReason())
                .update();
    }

    @Override
    public void completed(IssueDemandCompletedDTO dto) {
        CmtIssueDemand issueDemand = this.getById(dto.getId());
        if (Objects.isNull(issueDemand)) {
            throw new BizException(ApiMessage.NOT_FOUND);
        }
        this.lambdaUpdate().eq(CmtIssueDemand::getId, issueDemand.getId())
                .set(CmtIssueDemand::getStatus, CmtLocalConstants.ISSUE_DEMAND_STATUS.PENDING_ACCEPT)
                .set(CmtIssueDemand::getResultFeedback, dto.getResultFeedback())
                .set(CmtIssueDemand::getActualFinishTime, LocalDateTime.now())
                .update();
        // 发送企微通知给提出人验收
        WeComCardMessageBO message = new WeComCardMessageBO();
        String category = CmtLocalConstants.ISSUE_DEMAND_TYPE.DEMAND.equals(issueDemand.getType()) ? "需求开发" : "系统优化";
        String title = category + "验收通知";
        String description = StrUtil.format("""
                        <div class="gray">{}</div>
                        <div class="normal">你提出的一条{}已完成开发</div>
                        <div class="highlight">请与24小时内完成验收，否则将视为逾期</div>
                        """,
                LocalDateTime.now().format(GlobalConstants.DateFormat.Y_M_D_H_M),
                category);
        String url = coadeProperties.getCmt().getDomain() + "/issue-hub/" + issueDemand.getId();
        message.setTextcard(new WeComCardMessageBO.Content(title, description, url, "查看详情"));
        message.setAgentid(coadeProperties.getCmt().getWeComAgentId());
        weComService.sendMarkdownMessage(issueDemand.getProposeUserId(), message);
    }

    @Override
    public List<SelectionVO<String, String>> getPrincipalSelection() {
        List<CmtUser> users = userService.lambdaQuery()
                .select(CmtUser::getId, CmtUser::getUsername)
                .like(CmtUser::getDept, "信息部")
                .list();
        if (users.isEmpty()) {
            return List.of();
        }
        return users.stream().map(item -> new SelectionVO<>(item.getId(), item.getUsername())).toList();
    }

    @Override
    public void acceptancePass(String id) {
        CmtIssueDemand issueDemand = this.getById(id);
        if (Objects.isNull(issueDemand)) {
            throw new BizException(ApiMessage.NOT_FOUND);
        }
        this.lambdaUpdate().eq(CmtIssueDemand::getId, issueDemand.getId())
                .set(CmtIssueDemand::getStatus, CmtLocalConstants.ISSUE_DEMAND_STATUS.COMPLETED)
                .set(CmtIssueDemand::getAcceptanceTime, LocalDateTime.now())
                .update();
    }

    @Override
    public void voided(IssueDemandVoidedDTO dto) {
        CmtIssueDemand issueDemand = this.getById(dto.getId());
        if (Objects.isNull(issueDemand)) {
            throw new BizException(ApiMessage.NOT_FOUND);
        }
        this.lambdaUpdate().eq(CmtIssueDemand::getId, issueDemand.getId())
                .set(CmtIssueDemand::getStatus, CmtLocalConstants.ISSUE_DEMAND_STATUS.VOIDED)
                .set(CmtIssueDemand::getVoidedReason, dto.getVoidedReason())
                .set(CmtIssueDemand::getVoidedTime, LocalDateTime.now())
                .update();
    }

    @Override
    public void acceptanceReturn(IssueDemandAcceptanceReturnDTO dto) {
        CmtIssueDemand issueDemand = this.getById(dto.getId());
        if (Objects.isNull(issueDemand)) {
            throw new BizException(ApiMessage.NOT_FOUND);
        }
        String description = StrUtil.format(
                "{}\n\n补充内容：\n{}",
                issueDemand.getDescription(),
                dto.getDescription()
        );
        attachmentService.saveAttachmentsOwner(dto.getAttachmentIds(), AttachmentOwnerType.CMT_ISSUE_DEMAND, issueDemand.getId());
        this.lambdaUpdate().eq(CmtIssueDemand::getId, issueDemand.getId())
                .set(CmtIssueDemand::getStatus, CmtLocalConstants.ISSUE_DEMAND_STATUS.ASSESSING)
                .set(CmtIssueDemand::getExpectedFinishTime, dto.getExpectedFinishTime())
                .set(CmtIssueDemand::getActualFinishTime, null)
                .set(CmtIssueDemand::getResultFeedback, null)
                .set(CmtIssueDemand::getPlanFinishTime, null)
                .set(CmtIssueDemand::getDescription, description)
                .update();
    }


}
