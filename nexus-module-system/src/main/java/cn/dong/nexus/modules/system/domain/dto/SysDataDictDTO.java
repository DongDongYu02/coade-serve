package cn.dong.nexus.modules.system.domain.dto;

import cn.dong.nexus.core.base.BaseDTO;
import cn.dong.nexus.core.valid.BizValidate;
import cn.dong.nexus.modules.system.domain.entity.SysDataDict;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "数据字典 DTO")
public class SysDataDictDTO extends BaseDTO<SysDataDict> {

    @NotBlank
    @Schema(description = "字典名称")
    @BizValidate.Unique(message = "字典名称已存在！")
    private String name;

    @NotBlank
    @Schema(description = "字典编码")
    @BizValidate.Unique(message = "字典编码已存在！")
    private String code;

    @NotNull
    @Schema(description = "排序")
    private Integer sort;

}
