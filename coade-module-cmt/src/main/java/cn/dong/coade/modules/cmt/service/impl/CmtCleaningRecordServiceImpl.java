package cn.dong.coade.modules.cmt.service.impl;

import cn.dong.coade.modules.cmt.domain.dto.CleaningRecordDTO;
import cn.dong.coade.modules.cmt.domain.entity.CmtCleaningRecord;
import cn.dong.coade.modules.cmt.domain.query.CleaningRecordQuery;
import cn.dong.coade.modules.cmt.domain.vo.CleaningRecordVO;
import cn.dong.coade.modules.cmt.mapper.CmtCleaningRecordMapper;
import cn.dong.coade.modules.cmt.service.ICmtCleaningRecordService;
import cn.dong.nexus.common.api.AttachmentCommonApi;
import cn.dong.nexus.common.constants.AttachmentOwnerType;
import cn.dong.nexus.common.domain.bo.AttachmentBO;
import cn.dong.nexus.common.domain.vo.AttachmentVO;
import cn.dong.nexus.core.security.context.IAuthContext;
import cn.dong.nexus.core.security.context.LoginUser;
import cn.dong.nexus.core.util.PageUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CmtCleaningRecordServiceImpl extends ServiceImpl<CmtCleaningRecordMapper, CmtCleaningRecord> implements ICmtCleaningRecordService {

    private final AttachmentCommonApi attachmentCommonApi;
    private final IAuthContext authContext;

    @Override
    public IPage<CleaningRecordVO> getPageList(CleaningRecordQuery query) {
        Page<CmtCleaningRecord> page = this.page(query.toPage(), query.toQueryWrapper());
        if (page.getRecords().isEmpty()) {
            return PageUtil.emptyPage();
        }
        IPage<CleaningRecordVO> result = PageUtil.convertPage(page, CleaningRecordVO.class);
        List<AttachmentBO> attachmentBos = attachmentCommonApi.getByOwners(AttachmentOwnerType.CMT_CLEANING_RECORD, result.getRecords().stream().map(CleaningRecordVO::getId).toList());
        Map<String, List<AttachmentBO>> attOwnerGroupMap = attachmentBos.stream().collect(Collectors.groupingBy(AttachmentBO::getOwnerId));
        result.getRecords().forEach(item -> {
            List<AttachmentBO> attachments = attOwnerGroupMap.getOrDefault(item.getId(), List.of());
            item.setAttachments(BeanUtil.copyToList(attachments, AttachmentVO.class));
        });
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(CleaningRecordDTO dto) {
        LoginUser loginUser = authContext.getLoginUser();
        CmtCleaningRecord entity = dto.toEntity();
        entity.setPrincipalUserId(loginUser.getId());
        entity.setPrincipalUserName(loginUser.getUsername());
        this.save(entity);
        attachmentCommonApi.saveAttachmentsOwner(dto.getAttachmentIds(), AttachmentOwnerType.CMT_CLEANING_RECORD, entity.getId());
    }

    @Override
    public List<CleaningRecordVO> getList(CleaningRecordQuery query) {
        List<CmtCleaningRecord> list = this.list(query.toQueryWrapper());
        if (list.isEmpty()) {
            return List.of();
        }
        List<CleaningRecordVO> result = BeanUtil.copyToList(list, CleaningRecordVO.class);
        List<AttachmentBO> attachmentBos = attachmentCommonApi.getByOwners(AttachmentOwnerType.CMT_CLEANING_RECORD, result.stream().map(CleaningRecordVO::getId).toList());
        Map<String, List<AttachmentBO>> attOwnerGroupMap = attachmentBos.stream().collect(Collectors.groupingBy(AttachmentBO::getOwnerId));
        result.forEach(item -> {
            List<AttachmentBO> attachments = attOwnerGroupMap.getOrDefault(item.getId(), List.of());
            item.setAttachments(BeanUtil.copyToList(attachments, AttachmentVO.class));
        });
        return result;
    }
}
