package cn.dong.nexus.modules.rbac.service.impl;

import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.core.api.ApiMessage;
import cn.dong.nexus.core.base.BaseEntity;
import cn.dong.nexus.core.exception.BizException;
import cn.dong.nexus.core.security.context.IAuthContext;
import cn.dong.nexus.core.security.context.LoginUser;
import cn.dong.nexus.core.security.enums.Client;
import cn.dong.nexus.core.security.enums.SysUserIdentity;
import cn.dong.nexus.core.security.utils.PasswordUtil;
import cn.dong.nexus.core.security.vo.LoginUserVO;
import cn.dong.nexus.modules.rbac.domain.dto.ChangePasswordDTO;
import cn.dong.nexus.modules.rbac.domain.dto.LoginDTO;
import cn.dong.nexus.modules.rbac.domain.entity.SysPermission;
import cn.dong.nexus.modules.rbac.domain.entity.SysRole;
import cn.dong.nexus.modules.rbac.domain.entity.SysUser;
import cn.dong.nexus.modules.rbac.domain.entity.SysUserRole;
import cn.dong.nexus.modules.rbac.domain.vo.UserPermissionVO;
import cn.dong.nexus.modules.rbac.service.*;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.tree.Tree;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class SysAuthServiceImpl implements ISysAuthService {
    private final IAuthContext authContext;
    private final ISysUserService userService;
    private final ISysPermissionService sysPermissionService;
    private final ISysUserRoleService sysUserRoleService;
    private final ISysRolePermissionService sysRolePermissionService;
    private final ISysRoleService sysRoleService;

    @Override
    public LoginUserVO login(LoginDTO dto) {
        SysUser user = userService.lambdaQuery().eq(SysUser::getUsername, dto.getUsername()).one();
        if (Objects.isNull(user)) {
            throw new BizException("用户名不存在！");
        }
        boolean isMatch = PasswordUtil.matches(dto.getPassword(), user.getPassword());
        if (!isMatch) {
            throw new BizException("用户名或密码错误！");
        }
        LoginUser loginUser = BeanUtil.copyProperties(user, LoginUser.class);
        loginUser.setClient(Client.ADMIN.getCode());
        authContext.login(loginUser, Client.ADMIN);
        String token = authContext.getToken();
        LoginUserVO.UserInfo userInfo = BeanUtil.copyProperties(loginUser, LoginUserVO.UserInfo.class);
        return new LoginUserVO()
                .setToken(token)
                .setUserInfo(userInfo);
    }

    @Override
    public UserPermissionVO getLoginUserPermissions() {
        LoginUser loginUser = authContext.getLoginUser();
        if (SysUserIdentity.SUPER_ADMIN.getCode().equals(loginUser.getIdentity())) {
            List<SysPermission> list = sysPermissionService.lambdaQuery()
                    .orderByAsc(SysPermission::getSort)
                    .list();

            List<Tree<String>> permissionTree = sysPermissionService.buildPermissionTree(list);
            UserPermissionVO vo = new UserPermissionVO();
            vo.setIndex("/" + permissionTree.getFirst().get("path"));
            vo.setPermissions(permissionTree);
            vo.setAuthCodes(List.of("*"));
            return vo;
        }
        // 查询用户角色
        List<SysUserRole> userRoles = sysUserRoleService.lambdaQuery()
                .select(SysUserRole::getRoleId)
                .eq(SysUserRole::getUserId, loginUser.getId())
                .list();
        if (userRoles.isEmpty()) {
            return new UserPermissionVO().empty();
        }
        List<String> roleIds = userRoles.stream().map(SysUserRole::getRoleId).toList();
        boolean exists = sysRoleService.lambdaQuery().in(BaseEntity::getId, roleIds)
                .eq(SysRole::getStatus, GlobalConstants.ENABLE_STATUS.ENABLED)
                .exists();
        if(!exists){
            throw new BizException(ApiMessage.FORBIDDEN);
        }
        List<String> permissionIds = sysRolePermissionService.getPermissionIdsByRoleIds(roleIds);
        if (permissionIds.isEmpty()) {
            throw new BizException(ApiMessage.FORBIDDEN);
        }
        List<SysPermission> permissions = sysPermissionService.lambdaQuery()
                .in(SysPermission::getId, permissionIds)
                .eq(SysPermission::getStatus, GlobalConstants.ENABLE_STATUS.ENABLED)
                .eq(SysPermission::getHidden, GlobalConstants.INT_ZERO)
                .orderByAsc(SysPermission::getSort)
                .list();
        if (permissions.isEmpty()) {
           throw new BizException(ApiMessage.FORBIDDEN);
        }
        // 获取按钮操作权限
        List<String> autoCodes = permissions.stream().filter(item -> GlobalConstants.PERMISSION_TYPE.ACTION.equals(item.getType()))
                .map(SysPermission::getAuthCode)
                .toList();
        List<Tree<String>> permissionTree = sysPermissionService.buildPermissionTree(permissions);
        UserPermissionVO vo = new UserPermissionVO();
        vo.setIndex("/" + permissionTree.getFirst().get("path"));
        vo.setPermissions(permissionTree);
        vo.setAuthCodes(autoCodes);
        return vo;
    }

    @Override
    public void logout() {
        authContext.logout();
    }

    @Override
    public void changePassword(ChangePasswordDTO dto) {
        String userId = authContext.getLoginUser().getId();
        SysUser user = userService.lambdaQuery().select(SysUser::getPassword)
                .eq(BaseEntity::getId, userId).one();
        if (Objects.isNull(user)) {
            throw new BizException("用户不存在！");
        }
        boolean isMatch = PasswordUtil.matches(dto.getOldPassword(), user.getPassword());
        if (!isMatch) {
            throw new BizException("旧密码不正确！");
        }
        String newPasswordHash = PasswordUtil.encode(dto.getNewPassword());
        userService.lambdaUpdate()
                .set(SysUser::getPassword, newPasswordHash)
                .eq(BaseEntity::getId, userId)
                .update();
    }


}
