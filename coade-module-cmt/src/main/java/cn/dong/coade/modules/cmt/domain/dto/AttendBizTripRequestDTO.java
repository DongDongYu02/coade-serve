package cn.dong.coade.modules.cmt.domain.dto;

import cn.dong.coade.modules.cmt.constants.CmtLocalConstants;
import cn.dong.coade.modules.cmt.domain.entity.CmtBizTripRequest;
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
@Schema(description = "出差申请 DTO")
public class AttendBizTripRequestDTO extends BaseDTO<CmtBizTripRequest> {


    @Schema(description = "外出开始时间")
    @NotNull
    @JsonFormat(pattern = GlobalConstants.DatePattern.Y_M_D_H_M, timezone = GlobalConstants.ZoneTime.GMT8)
    private LocalDateTime beginTime;

    @Schema(description = "外出结束时间")
    @NotNull
    @JsonFormat(pattern = GlobalConstants.DatePattern.Y_M_D_H_M, timezone = GlobalConstants.ZoneTime.GMT8)
    private LocalDateTime endTime;

    @Schema(description = "外出事由")
    @NotBlank
    private String reason;

    @Schema(hidden = true)
    private BigDecimal duration;

    @Schema(hidden = true)
    private String durationFormat;

    @Override
    public void doValidate() {
        super.doValidate();
        String userId = SpringUtil.getBean(IAuthContext.class).getLoginUserOrThrow().getId();
        // 判断请假区间内是否已有请假申请
        boolean exists = Db.lambdaQuery(CmtBizTripRequest.class)
                .in(CmtBizTripRequest::getStatus, CmtLocalConstants.ATTEND_REQUEST_STATUS.PENDING, CmtLocalConstants.ATTEND_REQUEST_STATUS.APPROVED)
                .le(CmtBizTripRequest::getBeginTime, endTime)
                .ge(CmtBizTripRequest::getEndTime, beginTime)
                .eq(CmtBizTripRequest::getUserId, userId)
                .exists();
        if (exists) {
            throw new BizException("选择的出差时间段内已经提交过申请了！");
        }
    }
}
