package cn.dong.nexus.modules.rbac.service;

import cn.dong.nexus.modules.rbac.domain.entity.SysUserRole;
import cn.dong.nexus.modules.rbac.domain.vo.UserRoleDTO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ISysUserRoleService extends IService<SysUserRole> {

    void grantRoles(String userId, List<String> roleIds);

    UserRoleDTO getUserRoles(String id);
}
