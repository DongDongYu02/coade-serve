package cn.dong.nexus.modules.rbac.controller;

import cn.dong.nexus.core.api.Result;
import cn.dong.nexus.core.resmapping.annotation.ResultTranslate;
import cn.dong.nexus.core.valid.ValidGroup;
import cn.dong.nexus.modules.rbac.domain.dto.SysUserDTO;
import cn.dong.nexus.modules.rbac.domain.query.SysUserQuery;
import cn.dong.nexus.modules.rbac.domain.vo.SysUserVO;
import cn.dong.nexus.modules.rbac.domain.vo.detail.SysUserDetailVO;
import cn.dong.nexus.modules.rbac.service.ISysUserService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sys/user")
@Tag(name = "用户管理")
@RequiredArgsConstructor
public class SysUserController {
    private final ISysUserService service;

    /**
     * 分页列表
     *
     * @param query 条件
     * @author Dong
     **/
    @GetMapping
    @Operation(summary = "分页列表")
    public Result<IPage<SysUserVO>> pageList(@ParameterObject SysUserQuery query) {
        IPage<SysUserVO> pageList = service.getPageList(query);
        return Result.success(pageList);
    }

    @PostMapping
    @Operation(summary = "新增")
    public Result<Void> create(@RequestBody @Validated(ValidGroup.Create.class) SysUserDTO dto) {
        service.create(dto);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "编辑")
    public Result<Void> update(@RequestBody @Validated(ValidGroup.Update.class) SysUserDTO dto) {
        service.update(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除")
    public Result<Void> delete(@PathVariable String id) {
        service.deleteById(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "详情")
    @ResultTranslate
    public Result<SysUserDetailVO> getDetail(@PathVariable String id) {
        SysUserDetailVO detail = service.getDetailById(id);
        return Result.success(detail);
    }

    @PostMapping("/reset-password/{id}")
    @Operation(summary = "重置密码")
    public Result<String> resetPassword(@PathVariable String id) {
        String password = service.resetPassword(id);
        return Result.success(password, "重置成功");
    }


}