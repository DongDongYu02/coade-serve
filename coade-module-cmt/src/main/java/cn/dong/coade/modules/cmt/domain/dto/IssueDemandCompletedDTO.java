package cn.dong.coade.modules.cmt.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "问题需求处理完成 DTO")
public class IssueDemandCompletedDTO {

    @Schema(description = "住建")
    private String id;

    @Schema(description = "结果反馈")
    private String resultFeedback;
}
