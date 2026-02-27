package cn.dong.nexus.modules.rbac.service;

import cn.dong.nexus.modules.rbac.domain.dto.SysPermissionDTO;
import cn.dong.nexus.modules.rbac.domain.entity.SysPermission;
import cn.dong.nexus.modules.rbac.domain.query.PermissionTreeSelectionQuery;
import cn.dong.nexus.modules.rbac.domain.query.SysPermissionQuery;
import cn.dong.nexus.modules.rbac.domain.vo.detail.SysPermissionDetailVO;
import cn.dong.nexus.modules.rbac.domain.vo.SysPermissionVO;
import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ISysPermissionService extends IService<SysPermission> {


    /**
     * 新增权限
     *
     * @param dto 权限信息
     */
    void create(SysPermissionDTO dto);

    /**
     * 更新权限
     *
     * @param dto 权限信息
     */
    void update(SysPermissionDTO dto);

    /**
     * 权限树列表
     *
     * @param query 查询参数
     */
    List<SysPermissionVO> getTreeList(SysPermissionQuery query);

    /**
     * 根据ID 删除权限
     *
     * @param id 权限 ID
     */
    void deleteById(Long id);


    /**
     * 获取权限详情
     *
     * @param id 权限 ID
     * @return 权限详情
     */
    SysPermissionDetailVO getDetailById(String id);

    /**
     * 获取权限树选择
     *
     * @param query 权限类型
     * @return 权限树选择列表
     */
    List<Tree<String>> getSelectionTreeByTypes(PermissionTreeSelectionQuery query);


    List<Tree<String>> buildPermissionTree(List<SysPermission> permissions);


}
