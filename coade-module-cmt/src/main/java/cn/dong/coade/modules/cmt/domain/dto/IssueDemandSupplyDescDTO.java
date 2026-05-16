package cn.dong.coade.modules.cmt.domain.dto;

import cn.dong.nexus.common.constants.GlobalConstants;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "补充描述 DTO")
public class IssueDemandSupplyDescDTO {

    @Schema(description = "ID")
    @NotBlank
    private String id;

    @Schema(description = "补充内容")
    @NotBlank
    private String description;

    @Schema(description = "补充附件IDs")
    private List<String> attachmentIds;

    @Schema(description = "期望完成时间")
    @JsonFormat(pattern = GlobalConstants.DatePattern.NORMAL, timezone = GlobalConstants.ZoneTime.GMT8)
    @NotNull
    private LocalDateTime expectedFinishTime;
}
