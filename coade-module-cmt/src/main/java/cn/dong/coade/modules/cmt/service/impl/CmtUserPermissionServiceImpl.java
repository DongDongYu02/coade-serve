package cn.dong.coade.modules.cmt.service.impl;

import cn.dong.coade.modules.cmt.domain.entity.CmtPermission;
import cn.dong.coade.modules.cmt.domain.entity.CmtUserPermission;
import cn.dong.coade.modules.cmt.mapper.CmtUserPermissionMapper;
import cn.dong.coade.modules.cmt.service.ICmtPermissionService;
import cn.dong.coade.modules.cmt.service.ICmtUserPermissionService;
import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.core.security.context.IAuthContext;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CmtUserPermissionServiceImpl extends ServiceImpl<CmtUserPermissionMapper, CmtUserPermission> implements ICmtUserPermissionService {
    private final ICmtPermissionService cmtPermissionService;
    private final IAuthContext authContext;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grantBasicPermission(List<String> cmtUserIds) {
        // 获取基础权限
        List<CmtPermission> permissions = cmtPermissionService.lambdaQuery()
                .select(CmtPermission::getId)
                .eq(CmtPermission::getIsBasic, GlobalConstants.INT_YES)
                .list();
        if (permissions.isEmpty()) {
            return;
        }
        List<String> permissionIds = permissions.stream().map(CmtPermission::getId).toList();
        List<CmtUserPermission> userPermissions = new ArrayList<>();

        for (String cmtUserId : cmtUserIds) {
            for (String id : permissionIds) {
                userPermissions.add(new CmtUserPermission(cmtUserId, id));
            }
        }
        this.saveBatch(userPermissions);
    }

    @Override
    public List<String> getPermissionsByUserId(String userId, Integer userIdentity) {
        // 管理员全权
        if (GlobalConstants.UserIdentity.ADMIN.equals(userIdentity)) {
            List<CmtPermission> permissions = cmtPermissionService.lambdaQuery().select(CmtPermission::getCode)
                    .list();
            return permissions.stream().map(CmtPermission::getCode).toList();
        }
        // 无蓝凌关联用户
        if (GlobalConstants.UserIdentity.SPECIAL.equals(userIdentity)) {
            return List.of();
        }

        List<CmtUserPermission> userPermissions = this.lambdaQuery().select(CmtUserPermission::getCmtPermissionId)
                .eq(CmtUserPermission::getCmtUserId, userId)
                .list();
        if (userPermissions.isEmpty()) {
            return List.of();
        }
        List<CmtPermission> permissions = cmtPermissionService.lambdaQuery().select(CmtPermission::getCode)
                .in(CmtPermission::getId, userPermissions.stream().map(CmtUserPermission::getCmtPermissionId).toList())
                .list();
        return permissions.stream().map(CmtPermission::getCode).toList();

    }

    @Override
    public void removeBasicPermission(List<String> cmtUserIds) {
        // 获取基础权限
        List<CmtPermission> permissions = cmtPermissionService.lambdaQuery()
                .select(CmtPermission::getId)
                .eq(CmtPermission::getIsBasic, GlobalConstants.INT_YES)
                .list();
        if (permissions.isEmpty()) {
            return;
        }
        List<String> permissionIds = permissions.stream().map(CmtPermission::getId).toList();
        this.lambdaUpdate().in(CmtUserPermission::getCmtPermissionId, permissionIds).remove();
    }
}
