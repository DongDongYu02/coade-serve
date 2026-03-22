package cn.dong.nexus.common.domain.dto;

import lombok.Data;

@Data
public class AttachmentDTO {

    private String id;

    private String name;

    private String originName;

    private String path;

    private String size;

    private String mime;
}
