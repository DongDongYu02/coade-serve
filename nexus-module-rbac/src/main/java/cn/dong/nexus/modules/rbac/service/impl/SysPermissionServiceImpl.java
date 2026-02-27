package cn.dong.nexus.modules.rbac.service.impl;

import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.core.api.ApiMessage;
import cn.dong.nexus.core.base.BaseEntity;
import cn.dong.nexus.core.exception.BizException;
import cn.dong.nexus.modules.rbac.domain.dto.SysPermissionDTO;
import cn.dong.nexus.modules.rbac.domain.entity.SysPermission;
import cn.dong.nexus.modules.rbac.domain.entity.SysRolePermission;
import cn.dong.nexus.modules.rbac.domain.query.PermissionTreeSelectionQuery;
import cn.dong.nexus.modules.rbac.domain.query.SysPermissionQuery;
import cn.dong.nexus.modules.rbac.domain.vo.detail.SysPermissionDetailVO;
import cn.dong.nexus.modules.rbac.domain.vo.SysPermissionVO;
import cn.dong.nexus.modules.rbac.mapper.SysPermissionMapper;
import cn.dong.nexus.modules.rbac.service.ISysPermissionService;
import cn.dong.nexus.modules.rbac.service.ISysRolePermissionService;
import cn.dong.nexus.modules.rbac.service.ISysRoleService;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements ISysPermissionService {

    @Override
    public void create(SysPermissionDTO dto) {
        dto.doValidate();
        this.save(dto.toEntity());
    }

    @Override
    public void update(SysPermissionDTO dto) {
        dto.doValidate();
        this.updateById(dto.toEntity());
    }

    @Override
    public List<SysPermissionVO> getTreeList(SysPermissionQuery query) {
        QueryWrapper<SysPermission> queryWrapper = query.toQueryWrapper();
        List<SysPermission> records = this.list(queryWrapper);
        if (records.isEmpty()) {
            return Collections.emptyList();
        }
        return BeanUtil.copyToList(records, SysPermissionVO.class);
    }

    @Override
    public void deleteById(Long id) {
        boolean exists = this.lambdaQuery().eq(SysPermission::getId, id).exists();
        if (!exists) {
            throw new BizException(ApiMessage.NOT_FOUND);
        }
        Long count = this.lambdaQuery().eq(SysPermission::getPid, id).count();
        if (count > 0) {
            throw new BizException("该权限存在下级权限，无法删除！");
        }
        this.removeById(id);
    }

    @Override
    public SysPermissionDetailVO getDetailById(String id) {
        SysPermission permission = this.getById(id);
        if (Objects.isNull(permission)) {
            throw new BizException(ApiMessage.NOT_FOUND);
        }
        return BeanUtil.copyProperties(permission, SysPermissionDetailVO.class);
    }

    @Override
    public List<Tree<String>> getSelectionTreeByTypes(PermissionTreeSelectionQuery query) {
        List<SysPermission> permissions = this.lambdaQuery()
                .in(SysPermission::getType, query.getIncludeTypes())
                .list();
        if (permissions.isEmpty()) {
            return Collections.emptyList();
        }
        return TreeUtil.build(permissions, GlobalConstants.ROOT_ID,
                GlobalConstants.TREE_NODE_CONFIG,
                (treeNode, tree) -> {
                    tree.setId(treeNode.getId());
                    tree.setParentId(treeNode.getPid());
                    tree.setName(treeNode.getName());
                    tree.putExtra("type", treeNode.getType());
                });
    }

    /**
     * 数据校验
     *
     * @param dto 权限信息
     */
    private void doValidate(SysPermissionDTO dto) {
        if (dto.isUpdate()) {
            boolean exists = this.lambdaQuery().eq(BaseEntity::getId, dto.getId()).exists();
            if (!exists) {
                throw new BizException(ApiMessage.NOT_FOUND);
            }
        }
        // 权限名称唯一校验
        Long count = this.lambdaQuery().eq(SysPermission::getName, dto.getName())
                .ne(dto.isUpdate(), SysPermission::getId, dto.getId())
                .count();
        if (count > 0) {
            throw new BizException("权限名称已存在！");
        }
    }

    /**
     * 构建权限树
     *
     * @param permissions 权限数据集
     * @author Dong
     * @date 17:58 2023/12/14
     **/
    @Override
    public List<Tree<String>> buildPermissionTree(List<SysPermission> permissions) {
        // 过滤出按钮权限
        List<SysPermission> actionPermission = permissions.stream()
                .filter(permission -> Objects.equals(permission.getType(), GlobalConstants.PERMISSION_TYPE.ACTION))
                .toList();
        // 根据按钮所属菜单分组
        Map<String, List<SysPermission>> menuActionMapping = actionPermission.stream()
                .collect(Collectors.groupingBy(SysPermission::getPid));
        // 过滤出菜单权限
        List<SysPermission> permissionList = permissions.stream()
                .filter(permission -> !Objects.equals(permission.getType(), GlobalConstants.PERMISSION_TYPE.ACTION))
                .toList();

        return TreeUtil.build(permissionList, GlobalConstants.ROOT_ID, GlobalConstants.TREE_NODE_CONFIG,
                (treeNode, tree) -> {
                    buildPermissionTreeNode(treeNode, tree);
                    // 获取该菜单下的按钮权限
                    List<SysPermission> actions = menuActionMapping.get(treeNode.getId());
                    tree.putExtra("meta", buildMateData(treeNode, actions));
                });
    }

    /**
     * 构建权限树节点
     *
     * @param permission 权限对象
     * @param tree       树节点对象
     * @author Dong
     * @date 17:57 2023/12/14
     **/
    public void buildPermissionTreeNode(SysPermission permission, Tree<String> tree) {
        tree.setId(permission.getId());
        tree.setParentId(permission.getPid());
        tree.setWeight(permission.getSort() * -1);
        tree.setName(permission.getName());
        tree.putExtra("path", permission.getPath());
        tree.putExtra("hidden", permission.getHidden());
        tree.putExtra("redirect", permission.getRedirect());
        tree.putExtra("component", permission.getComponent());
        tree.putExtra("type", permission.getType());
        tree.putExtra("sort", permission.getSort());
    }

    /**
     * 构建权限meta数据
     *
     * @param permission 权限对象
     * @param actions    按钮权限
     * @author Dong
     * @date 9:07 2023/12/15
     **/
    private JSONObject buildMateData(SysPermission permission, List<SysPermission> actions) {
        JSONObject meta = new JSONObject();
        meta.set("icon", permission.getIcon());
        meta.set("keepAlive", permission.getKeepAlive());
        meta.set("affix", permission.getAffix());
        if (CollUtil.isNotEmpty(actions)) {
            meta.set("permission", actions.stream().map(SysPermission::getAuthCode).toList());
        }
        return meta;

    }

}
