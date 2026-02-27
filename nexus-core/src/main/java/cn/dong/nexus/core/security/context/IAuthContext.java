package cn.dong.nexus.core.security.context;

import cn.dong.nexus.core.security.enums.Client;

import java.util.Set;

/**
 * 用户登录态上下文接口
 * 在security模块中实现，供业务模块调用
 * @author Dong
 */
public interface IAuthContext {

    LoginUser getLoginUser();

    void checkSession();

    void updateSession(LoginUser loginUser);

    void isSuperAdmin(Integer identity);

    void containsSuperAdmin(Set<Integer> identities);

    void login(LoginUser loginUser, Client client);

    String getToken();

    void logout();

    void kickout(String userId);
}
