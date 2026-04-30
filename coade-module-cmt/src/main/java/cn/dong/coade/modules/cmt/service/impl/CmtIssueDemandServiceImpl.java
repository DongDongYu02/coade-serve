package cn.dong.coade.modules.cmt.service.impl;

import cn.dong.coade.modules.cmt.constants.CmtLocalConstants;
import cn.dong.coade.modules.cmt.domain.bo.WeComCardMessageBO;
import cn.dong.coade.modules.cmt.domain.dto.IssueDemandAssessmentedDTO;
import cn.dong.coade.modules.cmt.domain.dto.IssueDemandCompletedDTO;
import cn.dong.coade.modules.cmt.domain.dto.IssueDemandDTO;
import cn.dong.coade.modules.cmt.domain.dto.IssueDemandRejectDTO;
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
import cn.dong.nexus.core.api.ApiMessage;
import cn.dong.nexus.core.base.SelectionVO;
import cn.dong.nexus.core.config.properties.CoadeProperties;
import cn.dong.nexus.core.exception.BizException;
import cn.dong.nexus.core.resmapping.ResMappingUtil;
import cn.dong.nexus.core.security.context.IAuthContext;
import cn.dong.nexus.core.util.PageUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return BeanUtil.copyToList(records, IssueDemandVO.class);
    }

    @Override
    public IssueDemandDetailVO getDetailById(String id) {
        CmtIssueDemand issueDemand = this.getById(id);
        if (Objects.isNull(issueDemand)) {
            throw new BizException(ApiMessage.NOT_FOUND);
        }
        IssueDemandDetailVO detail = BeanUtil.copyProperties(issueDemand, IssueDemandDetailVO.class);
        List<AttachmentBO> attachments = attachmentService.getByOwners(AttachmentOwnerType.CMT_ISSUE_DEMAND, List.of(detail.getId()));
        if (attachments.isEmpty()) {
            return detail;
        }
        detail.setAttachments(BeanUtil.copyToList(attachments, AttachmentVO.class));
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
                .set(CmtIssueDemand::getStatus, CmtLocalConstants.ISSUE_DEMAND_STATUS.ACCEPTED)
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
                .set(CmtIssueDemand::getStatus, CmtLocalConstants.ISSUE_DEMAND_STATUS.PENDING_COMFIRM)
                .set(CmtIssueDemand::getResultFeedback, dto.getResultFeedback())
                .set(CmtIssueDemand::getActualFinishTime, LocalDateTime.now())
                .update();
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
    public void confirmed(String id) {
        CmtIssueDemand issueDemand = this.getById(id);
        if (Objects.isNull(issueDemand)) {
            throw new BizException(ApiMessage.NOT_FOUND);
        }
        this.lambdaUpdate().eq(CmtIssueDemand::getId, issueDemand.getId())
                .set(CmtIssueDemand::getStatus, CmtLocalConstants.ISSUE_DEMAND_STATUS.COMPLETED)
                .update();
    }


}
