package cn.dong.nexus.common.api;

import cn.dong.nexus.common.constants.AttachmentOwnerType;
import cn.dong.nexus.common.domain.bo.AttachmentBO;
import cn.dong.nexus.common.domain.bo.AttachmentOwnerSaveBO;

import java.util.List;

/**
 * 跨模块公共附件服务接口
 */
public interface CommonAttachmentService {

    /**
     * 保存附件所属者关联
     *
     * @param attachmentIds 附件 ID集合
     * @param ownerType     所属者类型
     * @param ownerId       所属者 ID
     */
    void saveAttachmentsOwner(List<String> attachmentIds, AttachmentOwnerType ownerType, String ownerId);

    void saveAttachmentsOwner(List<AttachmentOwnerSaveBO> attachmentBOList);

    void save(AttachmentBO attachment);


    /**
     * 查询附件
     */
    List<AttachmentBO> getByIds(List<String> ids);

    AttachmentBO getBoById(String id);

    /**
     * 保存附件记录
     *
     * @param paths 附件相对路径集合
     */
    List<String> saveByPaths(List<String> paths);

    /**
     * 获取所属者的所有图片路径
     *
     */
    List<AttachmentBO> getByOwners(AttachmentOwnerType ownerType, List<String> ownerIds);

    /**
     * 根据id 删除附件
     * _不覆盖mp的方法
     */
    void _removeByIds(List<String> ids);
}
