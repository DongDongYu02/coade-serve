package cn.dong.coade.modules.cmt.service;

import cn.dong.coade.modules.cmt.domain.dto.WecomLoginDTO;
import cn.dong.nexus.core.security.vo.LoginUserVO;
import jakarta.servlet.http.HttpServletResponse;

public interface IWeComAuthService {

    LoginUserVO login(WecomLoginDTO dto, HttpServletResponse response);
}
