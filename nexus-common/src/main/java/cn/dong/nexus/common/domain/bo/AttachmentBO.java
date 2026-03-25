package cn.dong.nexus.common.domain.bo;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class AttachmentBO {

    private String id;

    private String name;

    private String originName;

    private String path;

    private Long size;

    private String mime;

    private String ownerId;

    private String ownerType;

    private LocalDateTime createTime;

    private String createBy;
}
