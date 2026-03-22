package cn.dong.coade.modules.cmt.domain.vo;

import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.common.domain.vo.AttachmentVO;
import cn.dong.nexus.core.resmapping.annotation.ResMapping;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "6S评审详情 VO")
public class Cmt6sReviewDetailVO {
    @Schema(description = "ID")
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

    @Schema(description = "负责人 ID")
    @ResMapping(sourceTable = GlobalConstants.TableName.CMT_USER, key = "ekpId",values = "username")
    private String responsiblePersonId;
    @Schema(description = "负责人")
    private String responsiblePersonName;

    @Schema(description = "创建时间")
    @JsonFormat(timezone = GlobalConstants.ZoneTime.GMT8, pattern = GlobalConstants.DatePattern.Y_M_D_H_M)
    private LocalDateTime createTime;

    @Schema(description = "评审素材")
    private List<AttachmentVO> materials;

    @Schema(description = "评审问题列表")
    private List<Problem> problems;


    @Schema(description = "评审问题")
    @Data
    public static class Problem {
        @Schema(description = "ID")
        private String id;

        @Schema(description = "问题描述")
        private String description;

        @Schema(description = "整改建议")
        private String suggestion;

        @Schema(description = "问题图片地址")
        private List<AttachmentVO> images;

        @Schema(description = "协助人 ID")
        @ResMapping(sourceTable = GlobalConstants.TableName.CMT_USER, key = "ekpId",values = "username",targets = "assisterName")
        private String assister;

        @Schema(description = "协助人名称")
        private String assisterName;
    }
}
