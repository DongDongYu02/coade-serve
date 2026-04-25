package cn.dong.coade.modules.cmt.domain.dto;

import cn.dong.coade.modules.cmt.constants.CmtLocalConstants;
import cn.dong.coade.modules.cmt.domain.entity.CmtOvertimeRequest;
import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.core.base.BaseDTO;
import cn.dong.nexus.core.exception.BizException;
import cn.dong.nexus.core.security.context.IAuthContext;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "加班申请 DTO")
public class AttendOvertimeRequestDTO extends BaseDTO<CmtOvertimeRequest> {

    @Schema(description = "加班日期")
    private LocalDate overtimeDate;

    @Schema(description = "开始时间")
    @NotNull
    @JsonFormat(pattern = GlobalConstants.DatePattern.TIME, timezone = GlobalConstants.ZoneTime.GMT8)
    private LocalTime beginTime;

    @Schema(description = "结束时间")
    @NotNull
    @JsonFormat(pattern = GlobalConstants.DatePattern.TIME, timezone = GlobalConstants.ZoneTime.GMT8)
    private LocalTime endTime;

    @Schema(description = "加班事由")
    @NotBlank
    private String reason;

    @Schema(hidden = true)
    private BigDecimal duration;

    @Override
    public void doValidate() {
        super.doValidate();
        String userId = SpringUtil.getBean(IAuthContext.class).getLoginUserOrThrow().getId();
        // 判断区间内是否已有加班申请
        boolean exists = Db.lambdaQuery(CmtOvertimeRequest.class)
                .in(CmtOvertimeRequest::getStatus, CmtLocalConstants.ATTEND_REQUEST_STATUS.PENDING, CmtLocalConstants.ATTEND_REQUEST_STATUS.APPROVED)
                .eq(CmtOvertimeRequest::getOvertimeDate,overtimeDate)
                .le(CmtOvertimeRequest::getBeginTime, endTime)
                .ge(CmtOvertimeRequest::getEndTime, beginTime)
                .eq(CmtOvertimeRequest::getUserId, userId)
                .exists();
        if (exists) {
            throw new BizException("选择的时间段内已经提交过加班申请了！");
        }
    }
}
