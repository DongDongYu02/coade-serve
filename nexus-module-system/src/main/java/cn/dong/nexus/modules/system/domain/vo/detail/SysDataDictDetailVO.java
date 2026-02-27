package cn.dong.nexus.modules.system.domain.vo.detail;

import cn.dong.nexus.core.base.BaseDetailVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "数据字典详情 VO")
public class SysDataDictDetailVO extends BaseDetailVO {

    @Schema(description = "字典名称")
    private String name;

    @Schema(description = "字典编码")
    private String code;

    @Schema(description = "排序")
    private Integer sort;

}
