package cn.dong.coade.modules.cmt.controller;

import cn.dong.coade.modules.cmt.domain.dto.WecomLoginDTO;
import cn.dong.coade.modules.cmt.service.ICmtUserPermissionService;
import cn.dong.coade.modules.cmt.service.IWeComAuthService;
import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.core.api.Result;
import cn.dong.nexus.core.security.context.IAuthContext;
import cn.dong.nexus.core.security.context.LoginUser;
import cn.dong.nexus.core.security.vo.LoginUserVO;
import cn.dong.nexus.infra.util.RedisUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/cmt/auth")
@Tag(name = "CMT授权登录")
@RequiredArgsConstructor
public class CmtAuthController {
    private final IWeComAuthService weComAuthService;
    private final IAuthContext authContext;
    private final ICmtUserPermissionService userPermissionService;

    @GetMapping("/checkLogin")
    @Operation(summary = "检查登录态")
    public Result<Void> checkLogin() {
        LoginUser loginUser = SpringUtil.getBean(IAuthContext.class).getLoginUserOrThrow();
        return Result.success();
    }

    @PostMapping("/wecom-login")
    @Operation(summary = "企微授权登录")
    public Result<LoginUserVO> weComLogin(@Validated @RequestBody WecomLoginDTO dto) {
        LoginUserVO result = weComAuthService.login(dto);
        return Result.success(result);
    }


    @PostMapping("/provide/ekp/accessToken")
    @Operation(summary = "供EKP调用本服务接口的 token")
    public JSONObject provideEkpAccessToken() {
        Object token = RedisUtil.get(GlobalConstants.CacheKey.EKP_PROVIDE_TOKEN);
        if (Objects.nonNull(token)) {
            return new JSONObject().set("access_token", token);
        }
        String accessToken = RandomUtil.randomString(16);
        RedisUtil.set(GlobalConstants.CacheKey.EKP_PROVIDE_TOKEN, accessToken, 60 * 60 * 24);
        return new JSONObject().set("access_token", accessToken);
    }

    @PostMapping("/permissions")
    @Operation(summary = "获取CMT用户权限")
    public Result<List<String>> getPermissions() {
        LoginUser loginUser = authContext.getLoginUser();
        List<String> permissions = userPermissionService.getPermissionsByUserId(loginUser.getId(), loginUser.getIdentity());
        return Result.success(permissions);
    }
}
