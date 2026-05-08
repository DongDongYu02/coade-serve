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
@Schema(description = "打扫记录 VO")
public class CleaningRecordVO {
    @Schema(description = "ID")
    private String id;

    @Schema(description = "区域ID")
    @ResMapping(sourceTable = GlobalConstants.TableName.CMT_CLEANING_AREA)
    private String areaId;

    @Schema(description = "区域名称")
    private String areaName;

    @Schema(description = "负责人ID")
    private String principalUserId;

    @Schema(description = "负责人名称")
    private String principalUserName;

    @Schema(description = "上传时间")
    @JsonFormat(timezone = GlobalConstants.ZoneTime.GMT8, pattern = GlobalConstants.DatePattern.Y_M_D_H_M)
    private LocalDateTime createTime;

    @Schema(description = "结果图片")
    private List<AttachmentVO> attachments;
}
