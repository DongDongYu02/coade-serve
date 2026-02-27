package cn.dong.nexus.security.context;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.dong.nexus.core.api.ApiMessage;
import cn.dong.nexus.core.exception.BizException;
import cn.dong.nexus.core.security.context.IAuthContext;
import cn.dong.nexus.core.security.context.LoginUser;
import cn.dong.nexus.core.security.enums.Client;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;

@Component
public class SaAuthContext implements IAuthContext {
    private static final String USER_INFO_KEY = "userInfo";

    @Override
    public LoginUser getLoginUser() {
        SaSession session = StpUtil.getSession();
        if (Objects.nonNull(session)) {
            Object loginUser = session.get(USER_INFO_KEY);
            if (Objects.nonNull(loginUser)) {
                return (LoginUser) loginUser;
            }
        }
        throw new BizException(ApiMessage.UNAUTHORIZED);
    }

    @Override
    public void checkSession() {
        SaSession session = StpUtil.getSession(false);
        if(Objects.isNull(session)){
            throw new BizException(ApiMessage.UNAUTHORIZED);
        }
    }

    @Override
    public void updateSession(LoginUser loginUser) {

    }

    @Override
    public void isSuperAdmin(Integer identity) {

    }

    @Override
    public void containsSuperAdmin(Set<Integer> identities) {

    }

    @Override
    public void login(LoginUser loginUser, Client client) {
        loginUser.setClient(client.getCode());
        SaLoginParameter saLoginParameter = new SaLoginParameter().setDeviceType(client.getCode());
        StpUtil.login(loginUser.getId(), saLoginParameter);
        StpUtil.getSession().set(USER_INFO_KEY, loginUser);
    }

    @Override
    public String getToken() {
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        return tokenInfo.getTokenValue();
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

    @Override
    public void kickout(String userId) {
        StpUtil.kickout(userId);
    }

}
