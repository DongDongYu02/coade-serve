package cn.dong.nexus.modules.system.domain.vo;

import cn.dong.nexus.common.constants.GlobalConstants;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "字典管理 VO")
public class  SysDataDictVO {

    @Schema(description = "字典 ID")
    private String id;

    @Schema(description = "字典名称")
    private String name;

    @Schema(description = "字典编码")
    private String code;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = GlobalConstants.DatePattern.NORMAL, timezone = GlobalConstants.ZoneTime.GMT8)
    private LocalDateTime createTime;
}
