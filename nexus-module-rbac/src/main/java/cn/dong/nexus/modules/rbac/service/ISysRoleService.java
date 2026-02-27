package cn.dong.nexus.modules.rbac.service;

import cn.dong.nexus.modules.rbac.domain.dto.RolePermissionGrantDTO;
import cn.dong.nexus.modules.rbac.domain.dto.SysRoleDTO;
import cn.dong.nexus.modules.rbac.domain.entity.SysRole;
import cn.dong.nexus.modules.rbac.domain.query.SysRoleQuery;
import cn.dong.nexus.modules.rbac.domain.vo.RoleSelectionVO;
import cn.dong.nexus.modules.rbac.domain.vo.SysRoleVO;
import cn.dong.nexus.modules.rbac.domain.vo.detail.SysRoleDetailVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ISysRoleService extends IService<SysRole> {

    /**
     * 分页列表
     */
    IPage<SysRoleVO> getPageList(SysRoleQuery query);

    void create(SysRoleDTO dto);

    void update(SysRoleDTO dto);

    void deleteById(String id);

    List<String> getRolePermissionsById(String roleId);

    void grantRolePermissions(RolePermissionGrantDTO dto);

    SysRoleDetailVO getRoleDetailById(String id);

    List<RoleSelectionVO> getRoleSelectionList();
}
