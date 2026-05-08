package cn.dong.coade.modules.cmt.domain.vo;

import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.core.resmapping.annotation.ResMapping;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "保洁区域 VO")
public class CleaningAreaVO {

    @Schema(description = "ID")
    private String id;

    @Schema(description = "区域名称")
    private String name;

    @Schema(description = "负责人ID")
    @ResMapping(sourceTable = GlobalConstants.TableName.CMT_USER, values = "username", targets = "principalUserName")
    private String principalUserId;

    @Schema(description = "负责人名称")
    private String principalUserName;

    @Schema(description = "打扫规则")
    private String rule;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    @JsonFormat(timezone = GlobalConstants.ZoneTime.GMT8, pattern = GlobalConstants.DatePattern.Y_M_D_H_M)
    private LocalDateTime createTime;
}
