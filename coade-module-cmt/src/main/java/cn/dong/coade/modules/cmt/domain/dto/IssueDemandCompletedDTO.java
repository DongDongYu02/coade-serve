package cn.dong.coade.modules.cmt.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "问题需求处理完成 DTO")
public class IssueDemandCompletedDTO {

    @Schema(description = "主键")
    @NotBlank
    private String id;

    @Schema(description = "结果反馈")
    @NotBlank
    private String resultFeedback;

    @Schema(description = "结果附件")
    private List<String> attachmentIds;
}
