package cn.dong.coade.modules.cmt.domain.vo;

import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.core.resmapping.annotation.ResMapping;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "6S评审记录 VO")
public class Cmt6sReviewVO {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "部门 ID")
    @ResMapping(sourceTable = GlobalConstants.TableName.CMT_DEPT)
    private String deptId;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = GlobalConstants.DatePattern.Y_M_D_H_M, timezone = GlobalConstants.ZoneTime.GMT8)
    private LocalDateTime createTime;


}
