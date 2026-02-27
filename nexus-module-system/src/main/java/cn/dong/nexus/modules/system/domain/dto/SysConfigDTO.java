package cn.dong.nexus.modules.system.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "系统配置 DTO")
public class SysConfigDTO {

    @Schema(description = "配置列表")
    @NotEmpty
    private List<Config> configs;

    @Data
    public static class Config {
        @Schema(description = "key")
        private String key;

        @Schema(description = "value")
        private String value;
    }
}
