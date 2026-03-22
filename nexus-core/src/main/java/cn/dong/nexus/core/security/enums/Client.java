package cn.dong.nexus.core.security.enums;

import lombok.Getter;

@Getter
public enum Client {

    /**
     * 管理后台
     */
    ADMIN("admin"),
    /**
     * 移动端
     */
    APP("app"),
    /**
     * CMT (企业微信)
     */
    CMT("cmt");

    private final String code;

    Client(String code) {
        this.code = code;
    }
}
