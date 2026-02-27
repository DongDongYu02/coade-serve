package cn.dong.nexus.core.security.context;

import cn.dong.nexus.core.security.enums.SysUserIdentity;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class LoginUser {

    private String id;

    private String username;

    private String avatar;

    private String phone;

    private String nickname;

    private String client;

    private Integer identity;

    private Integer status;


    public boolean isSuperAdmin() {
        return SysUserIdentity.SUPER_ADMIN.getCode().equals(this.identity);
    }
}
