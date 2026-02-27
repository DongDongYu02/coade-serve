package cn.dong.nexus.modules.rbac.controller;

import cn.dong.nexus.core.api.Result;
import cn.dong.nexus.core.resmapping.annotation.ResultTranslate;
import cn.dong.nexus.core.valid.ValidGroup;
import cn.dong.nexus.modules.rbac.domain.dto.RolePermissionGrantDTO;
import cn.dong.nexus.modules.rbac.domain.dto.SysRoleDTO;
import cn.dong.nexus.modules.rbac.domain.query.SysRoleQuery;
import cn.dong.nexus.modules.rbac.domain.vo.RoleSelectionVO;
import cn.dong.nexus.modules.rbac.domain.vo.SysRoleVO;
import cn.dong.nexus.modules.rbac.domain.vo.detail.SysRoleDetailVO;
import cn.dong.nexus.modules.rbac.service.ISysRoleService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sys/role")
@Tag(name = "角色管理")
@RequiredArgsConstructor
public class SysRoleController {
    private final ISysRoleService service;


    @GetMapping
    @Operation(summary = "分页列表")
    public Result<IPage<SysRoleVO>> pageList(@ParameterObject SysRoleQuery query) {
        IPage<SysRoleVO> pageList = service.getPageList(query);
        return Result.success(pageList);
    }

    @PostMapping
    @Operation(summary = "新增")
    public Result<Void> create(@RequestBody @Validated(ValidGroup.Create.class) SysRoleDTO dto) {
        service.create(dto);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "编辑")
    public Result<Void> update(@RequestBody @Validated(ValidGroup.Update.class) SysRoleDTO dto) {
        service.update(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除")
    public Result<Void> delete(@PathVariable String id) {
        service.deleteById(id);
        return Result.success();
    }

    @GetMapping("/{roleId}/permissions")
    @Operation(summary = "获取角色权限 ID列表")
    public Result<List<String>> getRolePermissionIds(@PathVariable String roleId) {
        List<String> permissionsIds = service.getRolePermissionsById(roleId);
        return Result.success(permissionsIds);
    }

    @PutMapping("/{roleId}/permissions")
    @Operation(summary = "分配角色权限")
    public Result<Void> grantRolePermissions(@PathVariable String roleId, @RequestBody RolePermissionGrantDTO dto) {
        dto.setRoleId(roleId);
        service.grantRolePermissions(dto);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取角色详情")
    @ResultTranslate
    public Result<SysRoleDetailVO> getRoleDetail(@PathVariable String id) {
        SysRoleDetailVO detail = service.getRoleDetailById(id);
        return Result.success(detail);
    }

    @GetMapping("/selection")
    @Operation(summary = "角色选择列表")
    public Result<List<RoleSelectionVO>> getRoleSelectionList() {
        List<RoleSelectionVO> selectionList = service.getRoleSelectionList();
        return Result.success(selectionList);
    }

}