package cn.dong.nexus.core.base;

import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.core.resmapping.annotation.ResMapping;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BaseDetailVO {

    @Schema(description = "ID")
    private String id;

    @Schema(description = "创建人")
    @ResMapping(targets = "creator", sourceTable = "sys_user", values = "nickname")
    private String createBy;

    @Schema(description = "创建人名称")
    private String creator;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = GlobalConstants.DatePattern.NORMAL, timezone = GlobalConstants.ZoneTime.GMT8)
    private LocalDateTime createTime;

    @Schema(description = "更新人")
    @ResMapping(targets = "updater", sourceTable = "sys_user", values = "nickname")
    private String updateBy;

    @Schema(description = "更新人名称")
    private String updater;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = GlobalConstants.DatePattern.NORMAL, timezone = GlobalConstants.ZoneTime.GMT8)
    private LocalDateTime updateTime;
}
