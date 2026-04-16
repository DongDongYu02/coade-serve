package cn.dong.coade.modules.cmt.domain.vo;

import cn.dong.nexus.common.constants.GlobalConstants;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "问题需求 VO")
public class IssueDemandVO {

    @Schema(description = "ID")
    private String id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "类型")
    private Integer type;

    @Schema(description = "所属系统id")
    private Integer systemType;

    @Schema(description = "其他系统名称")
    private String otherSystem;

    @Schema(description = "提出部门")
    private String proposeDept;

    @Schema(description = "提出人ID")
    private String proposeUserId;

    @Schema(description = "提出人名称")
    private String proposeUserName;

    @Schema(description = "期望完成时间")
    @JsonFormat(pattern = GlobalConstants.DatePattern.NORMAL_ONLY_DATE, timezone = GlobalConstants.ZoneTime.GMT8)
    private LocalDate expectedFinishTime;

    @Schema(description = "负责人Id")
    private String principalUserId;

    @Schema(description = "负责人名称")
    private String principalUserName;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "计划完成时间")
    @JsonFormat(pattern = GlobalConstants.DatePattern.NORMAL_ONLY_DATE, timezone = GlobalConstants.ZoneTime.GMT8)
    private LocalDate planFinishTime;


    @Schema(description = "实际完成时间")
    @JsonFormat(pattern = GlobalConstants.DatePattern.NORMAL_ONLY_DATE, timezone = GlobalConstants.ZoneTime.GMT8)
    private LocalDate actualFinishTime;


    @Schema(description = "创建时间")
    @JsonFormat(pattern = GlobalConstants.DatePattern.NORMAL, timezone = GlobalConstants.ZoneTime.GMT8)
    private LocalDateTime createTime;


}
