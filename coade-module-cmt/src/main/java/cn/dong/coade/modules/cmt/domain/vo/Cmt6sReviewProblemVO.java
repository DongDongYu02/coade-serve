package cn.dong.coade.modules.cmt.domain.vo;

import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.core.resmapping.annotation.ResMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "6S问题 VO")
public class Cmt6sReviewProblemVO {

    @Schema(description = "ID")
    private String id;

    @Schema(description = "评审记录ID")
    @ResMapping(sourceTable = GlobalConstants.TableName.CMT_6S_REVIEW,
            values = {"title", "responsiblePersonId", "deptId", "status"},
            targets = {"reviewTitle", "responsiblePersonId", "deptId", "status"})
    private String reviewId;

    @Schema(description = "记录标题")
    private String reviewTitle;

    @Schema(description = "负责人 EKP ID")
    @ResMapping(sourceTable = GlobalConstants.TableName.CMT_USER, key = "ekpId", values = "username")
    private String responsiblePersonId;
    @Schema(description = "负责人名称")
    private String responsiblePersonName;

    @Schema(description = "部门 ID")
    @ResMapping(sourceTable = GlobalConstants.TableName.CMT_DEPT)
    private String deptId;
    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "问题描述")
    private String description;

    @Schema(description = "整改建议")
    private String suggestion;

    @Schema(description = "协助人 EKP ID")
    @ResMapping(sourceTable = GlobalConstants.TableName.CMT_USER, key = "ekpId", values = "username", targets = "assisterName")
    private String assister;

    @Schema(description = "协助人名称")
    private String assisterName;

    @Schema(description = "截止时间 ")
    private LocalDateTime deadline;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "问题图片")
    private String problemImage;

    @Schema(description = "整改结果图片")
    private String rectifyImage;
}
