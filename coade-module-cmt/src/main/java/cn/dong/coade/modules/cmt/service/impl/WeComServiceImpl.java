package cn.dong.coade.modules.cmt.service.impl;

import cn.dong.coade.modules.cmt.domain.bo.CmtLoginUser;
import cn.dong.coade.modules.cmt.domain.bo.WeComCardMessageBO;
import cn.dong.coade.modules.cmt.domain.dto.WeComUserInfoDTO;
import cn.dong.coade.modules.cmt.domain.dto.WecomLoginDTO;
import cn.dong.coade.modules.cmt.domain.entity.CmtUser;
import cn.dong.coade.modules.cmt.service.ICmtUserService;
import cn.dong.coade.modules.cmt.service.IWeComService;
import cn.dong.coade.modules.cmt.utils.WeComApiUtil;
import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.core.api.ApiMessage;
import cn.dong.nexus.core.exception.BizException;
import cn.dong.nexus.core.security.context.IAuthContext;
import cn.dong.nexus.core.security.enums.Client;
import cn.dong.nexus.core.security.vo.LoginUserVO;
import cn.hutool.core.bean.BeanUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class WeComServiceImpl implements IWeComService {

    public final ICmtUserService cmtUserService;
    private final IAuthContext authContext;

    @Override
    public LoginUserVO login(WecomLoginDTO dto, HttpServletResponse response) {
        WeComUserInfoDTO weComUserInfo = WeComApiUtil.getUserInfo(dto.getCode());
//        WeComUserInfoDTO weComUserInfo = new WeComUserInfoDTO();
//        weComUserInfo.setAvatar("https://wework.qpic.cn/wwpic/850073_Rdvw2E97RC6aRUi_1667200914/0");
//        weComUserInfo.setUserId("LinAJuan");
        CmtUser user = cmtUserService.lambdaQuery().eq(CmtUser::getWeComId, weComUserInfo.getUserId()).one();
        if (Objects.isNull(user) || user.getIdentity().equals(GlobalConstants.UserIdentity.SPECIAL)) {
            String username = WeComApiUtil.getUsername(weComUserInfo.getUserId());
            // 用户还没有关联蓝凌
            user = new CmtUser();
            user.setWeComId(weComUserInfo.getUserId());
            user.setAvatar(weComUserInfo.getAvatar());
            user.setIdentity(GlobalConstants.UserIdentity.SPECIAL);
            user.setId(weComUserInfo.getUserId());
            user.setPhone(weComUserInfo.getMobile());
            user.setUsername(username);
            user.setEkpId("");
            user.setDept("入职流程审批中");
        }
        CmtLoginUser loginUser = BeanUtil.copyProperties(user, CmtLoginUser.class);
        loginUser.setAvatar(weComUserInfo.getAvatar());
        loginUser.setNickname(user.getUsername());
        loginUser.setClient(Client.CMT.getCode());
        Map<String, Object> extInfo = Map.of("weComId", user.getWeComId(), "ekpId", user.getEkpId(), "dept", user.getDept());
        loginUser.setDept(user.getDept());
        loginUser.setWeComId(user.getWeComId());
        loginUser.setEkpId(user.getEkpId());
        loginUser.setExtInfo(extInfo);
        authContext.login(loginUser, Client.CMT);
        String token = authContext.getToken();
        return new LoginUserVO()
                .setToken(token)
                .setUserInfo(loginUser);

    }

    @Override
    @Async
    public void sendMarkdownMessage(WeComCardMessageBO message) {
        WeComApiUtil.sendCardMessageToUser(message);
    }

    @Override
    @Async
    public void sendMarkdownMessage(String cmtUserId, WeComCardMessageBO message) {
        CmtUser user = cmtUserService.lambdaQuery().select(CmtUser::getWeComId)
                .eq(CmtUser::getId, cmtUserId)
                .one();
        if (Objects.isNull(user)) {
            throw new BizException(ApiMessage.USER_NOT_FOUND);
        }
        message.setTouser(user.getWeComId());
        WeComApiUtil.sendCardMessageToUser(message);
    }


}
