package cn.dong.coade.modules.cmt.service.impl;

import cn.dong.coade.modules.cmt.domain.dto.WeComUserInfoDTO;
import cn.dong.coade.modules.cmt.domain.dto.WecomLoginDTO;
import cn.dong.coade.modules.cmt.domain.entity.CmtUser;
import cn.dong.coade.modules.cmt.service.ICmtUserService;
import cn.dong.coade.modules.cmt.service.IWeComAuthService;
import cn.dong.coade.modules.cmt.utils.WeComApiUtil;
import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.core.security.context.IAuthContext;
import cn.dong.nexus.core.security.context.LoginUser;
import cn.dong.nexus.core.security.enums.Client;
import cn.dong.nexus.core.security.vo.LoginUserVO;
import cn.hutool.core.bean.BeanUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class WeComAuthServiceImpl implements IWeComAuthService {

    public final ICmtUserService cmtUserService;
    private final IAuthContext authContext;

    @Override
    public LoginUserVO login(WecomLoginDTO dto) {
        WeComUserInfoDTO weComUserInfo = WeComApiUtil.getUserInfo(dto.getCode());
//        WeComUserInfoDTO weComUserInfo = new WeComUserInfoDTO();
//        weComUserInfo.setAvatar("https://wework.qpic.cn/wwpic/850073_Rdvw2E97RC6aRUi_1667200914/0");
//        weComUserInfo.setUserId("KD00681");
        CmtUser user = cmtUserService.lambdaQuery().eq(CmtUser::getWeComId, weComUserInfo.getUserId()).one();
        if (Objects.isNull(user)) {
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
        LoginUser loginUser = BeanUtil.copyProperties(user, LoginUser.class);
        loginUser.setAvatar(weComUserInfo.getAvatar());
        loginUser.setNickname(user.getUsername());
        loginUser.setClient(Client.CMT.getCode());
        Map<String, Object> extInfo = Map.of("weComId", user.getWeComId(), "ekpId", user.getEkpId(), "dept", user.getDept());
        loginUser.setExtInfo(extInfo);
        authContext.login(loginUser, Client.CMT);
        String token = authContext.getToken();
        LoginUserVO.UserInfo userInfo = BeanUtil.copyProperties(loginUser, LoginUserVO.UserInfo.class);
        userInfo.setExtInfo(extInfo);
        return new LoginUserVO()
                .setToken(token)
                .setUserInfo(userInfo);

    }


}
