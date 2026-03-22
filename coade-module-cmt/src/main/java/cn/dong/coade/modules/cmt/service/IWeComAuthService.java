package cn.dong.coade.modules.cmt.service;

import cn.dong.coade.modules.cmt.domain.dto.WecomLoginDTO;
import cn.dong.nexus.core.security.vo.LoginUserVO;

public interface IWeComAuthService {

    LoginUserVO login(WecomLoginDTO dto);
}
