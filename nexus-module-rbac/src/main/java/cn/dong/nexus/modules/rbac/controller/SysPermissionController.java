package cn.dong.nexus.modules.rbac.controller;

import cn.dong.nexus.core.api.Result;
import cn.dong.nexus.core.base.BaseEntity;
import cn.dong.nexus.core.resmapping.annotation.ResultTranslate;
import cn.dong.nexus.modules.rbac.domain.dto.PermissionUpdateStatusDTO;
import cn.dong.nexus.modules.rbac.domain.dto.SysPermissionDTO;
import cn.dong.nexus.modules.rbac.domain.entity.SysPermission;
import cn.dong.nexus.modules.rbac.domain.query.PermissionTreeSelectionQuery;
import cn.dong.nexus.modules.rbac.domain.query.SysPermissionQuery;
import cn.dong.nexus.modules.rbac.domain.vo.detail.SysPermissionDetailVO;
import cn.dong.nexus.modules.rbac.domain.vo.SysPermissionVO;
import cn.dong.nexus.modules.rbac.service.ISysPermissionService;
import cn.hutool.core.lang.tree.Tree;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sys/permission")
@Tag(name = "权限管理")
@RequiredArgsConstructor
public class SysPermissionController {
    private final ISysPermissionService sysPermissionService;

    @GetMapping
    @Operation(summary = "权限树列表")
    public Result<List<SysPermissionVO>> getTreeList(@ParameterObject @Validated SysPermissionQuery query) {
        List<SysPermissionVO> treeList = sysPermissionService.getTreeList(query);
        return Result.success(treeList);
    }

    @PostMapping
    @Operation(summary = "新增权限")
    public Result<Void> create(@RequestBody @Validated SysPermissionDTO dto) {
        sysPermissionService.create(dto);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "更新权限")
    public Result<Void> update(@RequestBody @Validated SysPermissionDTO dto) {
        sysPermissionService.update(dto);
        return Result.success();
    }

    @PutMapping("/status/{id}/{status}")
    @Operation(summary = "更新权限状态")
    public Result<Void> updateStatus(@ParameterObject @Validated PermissionUpdateStatusDTO dto) {
        sysPermissionService.lambdaUpdate()
                .set(SysPermission::getStatus, dto.getStatus())
                .eq(BaseEntity::getId, dto.getId()).update();
        return Result.success();
    }

    @GetMapping("/tree-selection")
    @Operation(summary = "权限选择树")
    public Result<List<Tree<String>>> getTreeSelection(@ParameterObject @Validated PermissionTreeSelectionQuery query) {
        List<Tree<String>> treeSelection = sysPermissionService.getSelectionTreeByTypes(query);
        return Result.success(treeSelection);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除权限")
    public Result<List<Tree<String>>> getTreeSelection(@PathVariable Long id) {
        sysPermissionService.deleteById(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取权限详情")
    @ResultTranslate
    public Result<SysPermissionDetailVO> getById(@PathVariable String id) {
        SysPermissionDetailVO detail = sysPermissionService.getDetailById(id);
        return  Result.success(detail);
    }
}