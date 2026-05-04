package cn.dong.coade.modules.cmt.domain.dto;

import cn.dong.coade.modules.cmt.domain.entity.CmtIssueDemand;
import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.core.base.BaseDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "问题需求 DTO")
public class IssueDemandDTO extends BaseDTO<CmtIssueDemand> {

    @Schema(description = "标题")
    @NotBlank
    private String title;

    @Schema(description = "类型 1问题反馈 2需求开发")
    @NotNull
    private Integer type;

    @Schema(description = "所属系统 数据字典")
    @NotNull
    private Integer systemType;

    @Schema(description = "其他系统名称")
    private String otherSystem;

    @Schema(description = "问题/需求描述")
    @NotBlank
    private String description;

    @Schema(description = "期望完成时间")
    @JsonFormat(pattern = GlobalConstants.DatePattern.NORMAL, timezone = GlobalConstants.ZoneTime.GMT8)
    private LocalDateTime expectedFinishTime;

    @Schema(description = "附件ID")
    private List<String> attachmentIds;

    @Schema(description = "提出部门")
    @NotBlank
    private String proposeDept;

    @Schema(description = "提出人")
    @NotBlank
    private String proposeUserId;

    @Schema(description = "提出人名称")
    @NotBlank
    private String proposeUserName;

}
