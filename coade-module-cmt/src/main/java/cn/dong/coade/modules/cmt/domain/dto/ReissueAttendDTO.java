package cn.dong.coade.modules.cmt.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "用户补卡申请 DTO")
public class ReissueAttendDTO {

    @Schema(description = "实际打卡时间")
    @NotBlank
    private String checkinTime;

    @Schema(description = "规则打卡时间")
    @NotBlank
    private String ruleCheckinTime;

    @Schema(description = "补卡类型 缺卡、迟到、早退")
    @NotBlank
    private String reissueType;

    @Schema(description = "补卡原因")
    @NotBlank
    private String reason;

    @Schema(description = "本系统cmt用户id")
    private String cmtUserId;
}
