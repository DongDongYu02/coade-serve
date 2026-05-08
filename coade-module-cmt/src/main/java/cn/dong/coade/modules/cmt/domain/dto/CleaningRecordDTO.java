package cn.dong.coade.modules.cmt.domain.dto;

import cn.dong.coade.modules.cmt.domain.entity.CmtCleaningRecord;
import cn.dong.nexus.core.base.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "打扫记录 DTO")
public class CleaningRecordDTO extends BaseDTO<CmtCleaningRecord> {

    @Schema(description = "区域ID")
    @NotBlank
    private String areaId;

    @Schema(description = "打扫图片")
    @NotEmpty
    private List<String> attachmentIds;

    @Schema(description = "备注")
    private String remark;
}
