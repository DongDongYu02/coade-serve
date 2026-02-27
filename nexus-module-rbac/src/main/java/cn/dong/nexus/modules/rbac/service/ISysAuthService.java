package cn.dong.nexus.modules.rbac.service;

import cn.dong.nexus.core.security.vo.LoginUserVO;
import cn.dong.nexus.modules.rbac.domain.dto.ChangePasswordDTO;
import cn.dong.nexus.modules.rbac.domain.dto.LoginDTO;
import cn.dong.nexus.modules.rbac.domain.vo.UserPermissionVO;
import cn.hutool.core.lang.tree.Tree;

import java.util.List;

public interface ISysAuthService {

    LoginUserVO login(LoginDTO dto);

    UserPermissionVO getLoginUserPermissions();

    void logout();

    void changePassword(ChangePasswordDTO dto);
}
