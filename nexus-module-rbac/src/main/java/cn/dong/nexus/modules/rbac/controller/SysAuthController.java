package cn.dong.nexus.modules.rbac.controller;

import cn.dong.nexus.core.api.Result;
import cn.dong.nexus.core.security.vo.LoginUserVO;
import cn.dong.nexus.modules.rbac.domain.dto.ChangePasswordDTO;
import cn.dong.nexus.modules.rbac.domain.dto.LoginDTO;
import cn.dong.nexus.modules.rbac.domain.vo.UserPermissionVO;
import cn.dong.nexus.modules.rbac.service.ISysAuthService;
import cn.hutool.core.lang.tree.Tree;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sys/auth")
@Tag(name = "授权登录")
@RequiredArgsConstructor
public class SysAuthController {

    private final ISysAuthService authService;

    /**
     * 用户登录
     *
     * @author Dong
     **/
    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<LoginUserVO> login(@RequestBody @Validated LoginDTO dto) {
        LoginUserVO result = authService.login(dto);
        return Result.success(result);
    }

    @GetMapping("/user/permissions")
    @Operation(summary = "获取用户权限")
    public Result<UserPermissionVO> getUserPermissions() {
        UserPermissionVO vo = authService.getLoginUserPermissions();
        return Result.success(vo);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }

    @PostMapping("/change-password")
    @Operation(summary = "修改密码")
    public Result<Void> changePassword(@RequestBody @Validated ChangePasswordDTO dto) {
        authService.changePassword(dto);
        return Result.success();
    }
}