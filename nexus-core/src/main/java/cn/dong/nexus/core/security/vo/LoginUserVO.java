package cn.dong.nexus.core.security.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class LoginUserVO {

    @Schema(description = "Token")
    private String token;

    @Schema(description = "用户信息")
    private UserInfo userInfo;

    @Data
    public static class UserInfo {

        @Schema(description = "用户 ID")
        private String id;

        @Schema(description = "用户名")
        private String username;

        @Schema(description = "头像")
        private String avatar;

        @Schema(description = "手机号")
        private String phone;

        @Schema(description = "昵称")
        private String nickname;

        @Schema(description = "客户端")
        private String client;

        @Schema(description = "身份")
        private Integer identity;

    }
}
