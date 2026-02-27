package cn.dong.nexus.modules.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "系统设置 VO")
public class SysSettingVO {

    @Schema(description = "系统名称")
    private String systemName;

    @Schema(description = "系统 LOGO")
    private String systemLogo;

}
