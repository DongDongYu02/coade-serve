package cn.dong.coade.modules.cmt.domain.dto;

import cn.dong.coade.modules.cmt.domain.entity.PmsVehicleInfo;
import cn.dong.nexus.core.base.BaseDTO;
import cn.dong.nexus.core.valid.BizValidate;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "PMS 车牌录入 DTO")
public class PmsVehicleInfoDTO extends BaseDTO<PmsVehicleInfo> {
    @Schema(description = "车牌号")
    @NotBlank
    @BizValidate.Unique(message = "车牌号已存在", column = "plateno")
    private String plateNo;

    @Schema(description = "车主姓名")
    @NotBlank
    private String ownerName;

}
