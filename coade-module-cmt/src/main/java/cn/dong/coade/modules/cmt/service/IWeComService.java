package cn.dong.coade.modules.cmt.service;

import cn.dong.coade.modules.cmt.domain.bo.WeComCardMessageBO;
import cn.dong.coade.modules.cmt.domain.dto.WecomLoginDTO;
import cn.dong.nexus.core.security.vo.LoginUserVO;
import jakarta.servlet.http.HttpServletResponse;

public interface IWeComService {

    LoginUserVO login(WecomLoginDTO dto, HttpServletResponse response);

    void sendMarkdownMessage(WeComCardMessageBO message);

    void sendMarkdownMessage(String cmtUserId, WeComCardMessageBO message);
}
