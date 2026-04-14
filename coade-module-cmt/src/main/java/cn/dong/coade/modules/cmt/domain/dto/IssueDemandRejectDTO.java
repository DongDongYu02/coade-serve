package cn.dong.coade.modules.cmt.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "问题需求驳回 DTO")
public class IssueDemandRejectDTO {

    @Schema(description = "ID")
    @NotBlank
    private String id;

    @Schema(description = "驳回理由")
    @NotBlank
    private String reason;
}
