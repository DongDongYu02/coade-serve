package cn.dong.nexus.modules.rbac.service.impl;

import cn.dong.nexus.modules.rbac.domain.entity.SysRolePermission;
import cn.dong.nexus.modules.rbac.mapper.SysRolePermissionMapper;
import cn.dong.nexus.modules.rbac.service.ISysRolePermissionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class SysRolePermissionServiceImpl extends ServiceImpl<SysRolePermissionMapper, SysRolePermission> implements ISysRolePermissionService {

    @Override
    public List<String> getPermissionIdsByRoleId(String roleId) {
        List<SysRolePermission> rolePermissions = this.lambdaQuery()
                .select(SysRolePermission::getPermissionId)
                .eq(SysRolePermission::getRoleId, roleId)
                .list();
        if (rolePermissions.isEmpty()) {
            return Collections.emptyList();
        }
        return rolePermissions.stream()
                .map(SysRolePermission::getPermissionId)
                .toList();
    }

    @Override
    public List<String> getPermissionIdsByRoleIds(List<String> roleIds) {
        List<SysRolePermission> rolePermissions = this.lambdaQuery()
                .select(SysRolePermission::getPermissionId)
                .in(SysRolePermission::getRoleId, roleIds)
                .list();
        if (rolePermissions.isEmpty()) {
            return Collections.emptyList();
        }
        return rolePermissions.stream()
                .map(SysRolePermission::getPermissionId)
                .toList();
    }

}
