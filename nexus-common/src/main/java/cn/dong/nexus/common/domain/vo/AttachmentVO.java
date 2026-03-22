package cn.dong.nexus.common.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentVO {

    private String id;

    private String name;

    private String originName;

    private String path;

    private Long size;

    private String mime;

    public AttachmentVO(String id, String path) {
        this.id = id;
        this.path = path;
    }
}
