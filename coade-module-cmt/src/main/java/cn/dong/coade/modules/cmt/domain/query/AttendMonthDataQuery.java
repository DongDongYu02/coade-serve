package cn.dong.coade.modules.cmt.domain.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "月考勤数据查询对象")
public class AttendMonthDataQuery {

    @Schema(description = "用户ID")
    @NotBlank
    private List<String> userIds;

    @Schema(description = "年份")
    @NotNull
    private Integer year;

    @Schema(description = "月份")
    @NotNull
    private Integer month;
}
