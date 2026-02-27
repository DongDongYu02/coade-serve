package cn.dong.nexus.modules.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "字典项 VO")
public class SysDataDictItemVO {

    @Schema(description = "字典项 ID")
    private String id;

    @Schema(description = "字典 ID")
    private String dataDictId;

    @Schema(description = "字典项名称")
    private String text;

    @Schema(description = "字典项值")
    private String value;

    @Schema(description = "排序")
    private Integer sort;
}
