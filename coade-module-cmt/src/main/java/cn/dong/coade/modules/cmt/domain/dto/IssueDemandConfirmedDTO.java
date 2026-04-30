package cn.dong.coade.modules.cmt.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "问题需求确认完成 DTO")
public class IssueDemandConfirmedDTO {

    @Schema(description = "主键")
    private String id;
}
