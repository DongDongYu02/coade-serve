package cn.dong.coade.modules.cmt.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Schema(description = "6S评审状态统计 VO")
@NoArgsConstructor
@AllArgsConstructor
public class Cmt6sReviewStatusCountVO {

    @Schema(description = "待整改")
    private Long pendingRectify;

    @Schema(description = "整改完成")
    private Long rectifyCompleted;

    @Schema(description = "总数")
    private Long total;
}
