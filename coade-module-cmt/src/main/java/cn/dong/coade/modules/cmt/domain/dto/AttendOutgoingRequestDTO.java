package cn.dong.coade.modules.cmt.domain.dto;

import cn.dong.coade.modules.cmt.domain.entity.CmtOutgoingRequest;
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
@Schema(description = "请假申请 DTO")
public class AttendOutgoingRequestDTO extends BaseDTO<CmtOutgoingRequest> {

    @Schema(description = "外出日期")
    @NotNull
    @JsonFormat(pattern = GlobalConstants.DatePattern.NORMAL_ONLY_DATE, timezone = GlobalConstants.ZoneTime.GMT8)
    private LocalDate outDate;

    @Schema(description = "外出开始时间")
    @NotNull
    @JsonFormat(pattern = GlobalConstants.DatePattern.TIME, timezone = GlobalConstants.ZoneTime.GMT8)
    private LocalTime outTimeBegin;

    @Schema(description = "外出结束时间")
    @NotNull
    @JsonFormat(pattern = GlobalConstants.DatePattern.TIME, timezone = GlobalConstants.ZoneTime.GMT8)
    private LocalTime outTimeEnd;

    @Schema(description = "外出事由")
    @NotBlank
    private String reason;

    @Schema(hidden = true)
    private BigDecimal duration;

    @Override
    public void doValidate() {
        super.doValidate();
        String userId = SpringUtil.getBean(IAuthContext.class).getLoginUserOrThrow().getId();
        // 判断请假区间内是否已有外出申请
        boolean exists = Db.lambdaQuery(CmtOutgoingRequest.class)
                .le(CmtOutgoingRequest::getOutTimeBegin, outTimeEnd)
                .ge(CmtOutgoingRequest::getOutTimeEnd, outTimeBegin)
                .eq(CmtOutgoingRequest::getOutDate, outDate)
                .eq(CmtOutgoingRequest::getUserId, userId)
                .exists();
        if (exists) {
            throw new BizException("选择的外出时间段内已经提交过申请了！");
        }
    }
}
