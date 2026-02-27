package cn.dong.nexus.modules.rbac.service.impl;

import cn.dong.nexus.core.resmapping.ResMappingUtil;
import cn.dong.nexus.modules.rbac.domain.entity.SysRole;
import cn.dong.nexus.modules.rbac.domain.entity.SysUserRole;
import cn.dong.nexus.modules.rbac.domain.vo.UserRoleDTO;
import cn.dong.nexus.modules.rbac.mapper.SysUserRoleMapper;
import cn.dong.nexus.modules.rbac.service.ISysRoleService;
import cn.dong.nexus.modules.rbac.service.ISysUserRoleService;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements ISysUserRoleService {

    private void clearRolesByUserId(String userId) {
        this.lambdaUpdate().eq(SysUserRole::getUserId, userId).remove();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grantRoles(String userId, List<String> roleIds) {
        if (CollUtil.isEmpty(roleIds)) {
            return;
        }
        this.clearRolesByUserId(userId);
        List<SysUserRole> userRoles = roleIds.stream().map(roleId -> {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            return userRole;
        }).toList();
        this.saveBatch(userRoles);
    }

    @Override
    public UserRoleDTO getUserRoles(String userId) {
        List<SysUserRole> userRoles = this.lambdaQuery().select(SysUserRole::getRoleId)
                .eq(SysUserRole::getUserId, userId)
                .list();
        if (userRoles.isEmpty()) {
            return null;
        }
        List<String> roleIds = userRoles.stream().map(SysUserRole::getRoleId).toList();
        Map<String, String> roleMapping = ResMappingUtil.getFieldMapping(roleIds, SysRole::getId, SysRole::getName);
        UserRoleDTO dto = new UserRoleDTO();
        dto.setUserId(userId);
        dto.setRoleNames(roleMapping.values().stream().toList());
        dto.setRoleIds(roleIds);
        return dto;

    }
}
