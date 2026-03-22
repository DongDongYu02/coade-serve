package cn.dong.nexus.modules.system.domain.entity;

import lombok.Data;

@Data
public class SysAttachmentOwner {

    private String id;

    private String attachmentId;

    private String ownerId;

    private String ownerType;
}
