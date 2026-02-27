package cn.dong.nexus.modules.rbac.service;

import cn.dong.nexus.modules.rbac.domain.entity.SysRolePermission;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ISysRolePermissionService extends IService<SysRolePermission> {


    List<String> getPermissionIdsByRoleId(String roleId);

    List<String> getPermissionIdsByRoleIds(List<String> roleIds);
}
