package cn.dong.coade.modules.cmt.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Schema(description = "问题需求状态数量统计 VO")
@Accessors(chain = true)
public class IssueDemandStatusCountVO {

    @Schema(description = "总数")
    private Long total;

    @Schema(description = "处理中")
    private Long processing;

    @Schema(description = "已完成")
    private Long finished;

    @Schema(description = "已作废")
    private Long voided;


    public IssueDemandStatusCountVO empty() {
        total = 0L;
        processing = 0L;
        finished = 0L;
        return this;
    }
}
