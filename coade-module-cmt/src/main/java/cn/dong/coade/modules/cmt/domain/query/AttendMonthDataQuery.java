package cn.dong.coade.modules.cmt.domain.query;

import cn.dong.coade.modules.cmt.domain.entity.CmtUser;
import cn.dong.nexus.core.base.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "月考勤数据查询对象")
public class AttendMonthDataQuery extends PageQuery<CmtUser> {

    @Schema(description = "用户ID")
    private List<String> userIds;

    @Schema(description = "年份")
    @NotNull
    private Integer year;

    @Schema(description = "月份")
    @NotNull
    private Integer month;

    @Schema(description = "部门")
    private String dept;
}
