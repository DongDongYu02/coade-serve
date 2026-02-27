package cn.dong.nexus.modules.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "附件 VO")
public class AttachmentVO {

    @Schema(description = "文件名称")
    private String name;

    @Schema(description = "文件格式")
    private String mime;

    @Schema(description = "文件大小")
    private Long size;

    @Schema(description = "文件路径")
    private String path;

    @Schema(description = "文件访问地址")
    private String url;
}
