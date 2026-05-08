package cn.dong.coade.modules.cmt.domain.dto;

import cn.dong.coade.modules.cmt.domain.entity.CmtCleaningArea;
import cn.dong.nexus.core.base.BaseDTO;
import cn.dong.nexus.core.valid.BizValidate;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CleaningAreaDTO extends BaseDTO<CmtCleaningArea> {

    @Schema(description = "区域名称")
    @NotBlank
    @BizValidate.Unique(message = "区域名称已存在！")
    private String name;

    @Schema(description = "负责人ID")
    @NotBlank
    private String principalUserId;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "打扫规则")
    private String rule;
}
