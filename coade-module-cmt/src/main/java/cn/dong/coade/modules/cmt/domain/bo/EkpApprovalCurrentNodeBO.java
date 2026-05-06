package cn.dong.coade.modules.cmt.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Schema(description = "EKP流程当前审批节点 DTO")
@Accessors(chain = true)
public class EkpApprovalCurrentNodeBO {

    @Schema(description = "节点名称")
    private String nodeName;

    @Schema(description = "当前处理人")
    private String handler;
}
