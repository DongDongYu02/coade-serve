package cn.dong.nexus.modules.rbac.service.impl;

import cn.dong.nexus.core.api.ApiMessage;
import cn.dong.nexus.core.base.BaseEntity;
import cn.dong.nexus.core.exception.BizException;
import cn.dong.nexus.core.util.PageUtil;
import cn.dong.nexus.modules.rbac.domain.dto.RolePermissionGrantDTO;
import cn.dong.nexus.modules.rbac.domain.dto.SysRoleDTO;
import cn.dong.nexus.modules.rbac.domain.entity.SysRole;
import cn.dong.nexus.modules.rbac.domain.entity.SysRolePermission;
import cn.dong.nexus.modules.rbac.domain.entity.SysUserRole;
import cn.dong.nexus.modules.rbac.domain.query.SysRoleQuery;
import cn.dong.nexus.modules.rbac.domain.vo.RoleSelectionVO;
import cn.dong.nexus.modules.rbac.domain.vo.SysRoleVO;
import cn.dong.nexus.modules.rbac.domain.vo.detail.SysRoleDetailVO;
import cn.dong.nexus.modules.rbac.mapper.SysRoleMapper;
import cn.dong.nexus.modules.rbac.service.ISysRolePermissionService;
import cn.dong.nexus.modules.rbac.service.ISysRoleService;
import cn.dong.nexus.modules.rbac.service.ISysUserRoleService;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

    private final ISysRolePermissionService sysRolePermissionService;
    private final ISysUserRoleService sysUserRoleService;

    @Override
    public IPage<SysRoleVO> getPageList(SysRoleQuery query) {
        IPage<SysRole> page = this.page(query.toPage(), query.toQueryWrapper());
        IPage<SysRoleVO> pageVO = PageUtil.convertPage(page, SysRoleVO.class);
        return pageVO;
    }

    @Override
    public void create(SysRoleDTO dto) {
        dto.doValidate();
        this.save(dto.toEntity());
    }

    @Override
    public void update(SysRoleDTO dto) {
        dto.doValidate();
        this.updateById(dto.toEntity());
    }

    @Override
    public void deleteById(String id) {
        boolean exists = sysUserRoleService.lambdaQuery().eq(SysUserRole::getRoleId, id).exists();
        if (exists) {
            throw new BizException("删除失败：该角色存在相关用户！");
        }
        this.removeById(id);
    }

    @Override
    public List<String> getRolePermissionsById(String roleId) {
        return sysRolePermissionService.getPermissionIdsByRoleId(roleId);
    }

    @Override
    public void grantRolePermissions(RolePermissionGrantDTO dto) {
        // 先删除已有的角色权限关联
        sysRolePermissionService.lambdaUpdate()
                .eq(SysRolePermission::getRoleId, dto.getRoleId())
                .remove();
        // 批量新增角色权限关联
        List<SysRolePermission> rolePermissions = dto.getPermissionIds().stream().map(item -> {
            SysRolePermission rolePermission = new SysRolePermission();
            rolePermission.setRoleId(dto.getRoleId());
            rolePermission.setPermissionId(item);
            return rolePermission;
        }).toList();
        sysRolePermissionService.saveBatch(rolePermissions);
    }

    @Override
    public SysRoleDetailVO getRoleDetailById(String id) {
        SysRole sysRole = this.getById(id);
        if (sysRole == null) {
            throw new BizException(ApiMessage.NOT_FOUND);
        }
        return BeanUtil.copyProperties(sysRole, SysRoleDetailVO.class);
    }

    @Override
    public List<RoleSelectionVO> getRoleSelectionList() {
        List<SysRole> roles = this.lambdaQuery()
                .select(BaseEntity::getId, SysRole::getName, SysRole::getStatus)
                .list();
        return roles.stream()
                .map(item -> BeanUtil.copyProperties(item, RoleSelectionVO.class))
                .toList();
    }


}
