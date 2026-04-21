package cn.dong.coade.modules.cmt.domain.dto;

import cn.dong.coade.modules.cmt.domain.entity.CmtLeaveRequest;
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
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "请假申请 DTO")
public class AttendLeaveRequestDTO extends BaseDTO<CmtLeaveRequest> {

    @Schema(description = "请假类型 2事假 3病假")
    @NotNull
    private Integer type;

    @Schema(description = "请假开始时间")
    @NotNull
    @JsonFormat(pattern = GlobalConstants.DatePattern.Y_M_D_H_M, timezone = GlobalConstants.ZoneTime.GMT8)
    private LocalDateTime beginTime;

    @Schema(description = "请假结束时间")
    @NotNull
    @JsonFormat(pattern = GlobalConstants.DatePattern.Y_M_D_H_M, timezone = GlobalConstants.ZoneTime.GMT8)
    private LocalDateTime endTime;

    @Schema(description = "请假原因")
    @NotBlank
    private String reason;

    @Schema(hidden = true)
    private BigDecimal duration;

    @Override
    public void doValidate() {
        super.doValidate();
        String userId = SpringUtil.getBean(IAuthContext.class).getLoginUserOrThrow().getId();
        // 判断请假区间内是否已有请假申请
        boolean exists = Db.lambdaQuery(CmtLeaveRequest.class)
                .le(CmtLeaveRequest::getBeginTime, endTime)
                .ge(CmtLeaveRequest::getEndTime, beginTime)
                .eq(CmtLeaveRequest::getUserId, userId)
                .exists();
        if (exists) {
            throw new BizException("选择的请假时间段内已经提交过请假申请了！");
        }
    }
}
