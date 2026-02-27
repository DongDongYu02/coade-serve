package cn.dong.nexus.core.security.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 系统用户身份枚举
 */
@Getter
@AllArgsConstructor
public enum SysUserIdentity {

    /**
     * 超管（一般为开发或运维）
     */
    SUPER_ADMIN(1),

    /**
     * 普通用户（系统管理员...普通用户）
     */
    NORMAL(2);

    private final Integer code;

    public static SysUserIdentity get(Integer code) {
        for (SysUserIdentity value : SysUserIdentity.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }

}
