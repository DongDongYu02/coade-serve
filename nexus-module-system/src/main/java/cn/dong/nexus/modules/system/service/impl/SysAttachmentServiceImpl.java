package cn.dong.nexus.modules.system.service.impl;

import cn.dong.nexus.common.api.CommonAttachmentService;
import cn.dong.nexus.common.constants.AttachmentOwnerType;
import cn.dong.nexus.common.domain.bo.AttachmentBO;
import cn.dong.nexus.common.domain.bo.AttachmentOwnerSaveBO;
import cn.dong.nexus.core.exception.BizException;
import cn.dong.nexus.modules.system.domain.entity.SysAttachment;
import cn.dong.nexus.modules.system.domain.entity.SysAttachmentOwner;
import cn.dong.nexus.modules.system.domain.vo.AttachmentVO;
import cn.dong.nexus.modules.system.mapper.SysAttachmentMapper;
import cn.dong.nexus.modules.system.service.ISysAttachmentOwnerService;
import cn.dong.nexus.modules.system.service.ISysAttachmentService;
import cn.dong.nexus.modules.system.util.UploadUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SysAttachmentServiceImpl extends ServiceImpl<SysAttachmentMapper, SysAttachment> implements ISysAttachmentService, CommonAttachmentService {

    private final ISysAttachmentOwnerService attachmentOwnerService;

    @Value("${nexus.file-upload-path}")
    private String uploadPath;

    @Override
    public AttachmentVO create(MultipartFile file) {
        UploadUtil.UploadInfo uploadInfo = UploadUtil.saveMultipartFile(file);
        SysAttachment attachment = new SysAttachment();
        attachment.setName(uploadInfo.getFileName());
        attachment.setOriginName(uploadInfo.getOriginName());
        attachment.setMime(uploadInfo.getMime());
        attachment.setPath(uploadInfo.getRelativePath());
        attachment.setSize(uploadInfo.getSize());
        this.save(attachment);

        AttachmentVO vo = new AttachmentVO();
        vo.setMime(uploadInfo.getMime());
        vo.setName(uploadInfo.getOriginName());
        vo.setPath(uploadInfo.getRelativePath());
        vo.setSize(uploadInfo.getSize());
        vo.setId(attachment.getId());
        return vo;
    }

    @Override
    public void saveAttachmentsOwner(List<String> attachmentIds, AttachmentOwnerType ownerType, String ownerId) {
        if (CollUtil.isEmpty(attachmentIds)) {
            return;
        }
        this.lambdaUpdate().set(SysAttachment::getOwnerId, ownerId)
                .set(SysAttachment::getOwnerType, ownerType.getCode())
                .in(SysAttachment::getId, attachmentIds)
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAttachmentsOwner(List<AttachmentOwnerSaveBO> attachmentBOList) {
        if (CollUtil.isEmpty(attachmentBOList)) return;
        List<SysAttachment> attachments = BeanUtil.copyToList(attachmentBOList, SysAttachment.class);
        this.updateBatchById(attachments);
    }

    @Override
    public void save(AttachmentBO attachment) {
        SysAttachment entity = new SysAttachment();
        entity.setPath(attachment.getPath());
        entity.setOriginName(entity.getOriginName());
        entity.setName(attachment.getName());
        entity.setMime(attachment.getMime());
        entity.setSize(attachment.getSize());
        entity.setOwnerId(attachment.getOwnerId());
        entity.setOwnerType(attachment.getOwnerType());
        this.save(entity);
    }

    @Override
    public List<AttachmentBO> getByIds(List<String> ids) {
        if (CollUtil.isEmpty(ids)) {
            return List.of();
        }
        List<SysAttachment> records = this.lambdaQuery().in(SysAttachment::getId, ids).list();
        if (records.isEmpty()) {
            return List.of();
        }
        return BeanUtil.copyToList(records, AttachmentBO.class);
    }

    @Override
    public AttachmentBO getBoById(String id) {
        SysAttachment entity = this.getById(id);
        if (Objects.isNull(entity)) return null;
        return BeanUtil.copyProperties(entity, AttachmentBO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<String> saveByPaths(List<String> paths) {
        if (CollUtil.isEmpty(paths)) {
            throw new BizException("attachment save by paths error: paths is empty");
        }
        List<SysAttachment> entities = paths.stream().map(path -> {
            File file = FileUtil.file(uploadPath + path);
            SysAttachment entity = new SysAttachment();
            entity.setSize(FileUtil.size(file));
            entity.setName(FileUtil.getPrefix(path));
            entity.setOriginName(FileUtil.getPrefix(path));
            entity.setMime(FileUtil.getMimeType(path));
            entity.setPath(path);
            return entity;
        }).toList();
        this.saveBatch(entities);
        return entities.stream().map(SysAttachment::getId).toList();
    }

    @Override
    public List<AttachmentBO> getByOwners(AttachmentOwnerType ownerType, List<String> ownerIds) {
        List<SysAttachment> records = this.lambdaQuery()
                .eq(SysAttachment::getOwnerType, ownerType.getCode())
                .in(SysAttachment::getOwnerId, ownerIds)
                .list();
        if (records.isEmpty()) {
            return List.of();
        }
        return BeanUtil.copyToList(records, AttachmentBO.class);
    }

    @Override
    public void _removeByIds(List<String> ids) {
        this.removeByIds(ids);
    }
}
