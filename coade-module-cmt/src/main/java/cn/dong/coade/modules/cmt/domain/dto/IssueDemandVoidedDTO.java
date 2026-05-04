package cn.dong.coade.modules.cmt.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "问题需求作废 DTO")
public class IssueDemandVoidedDTO {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "结果反馈")
    private String voidedReason;
}
