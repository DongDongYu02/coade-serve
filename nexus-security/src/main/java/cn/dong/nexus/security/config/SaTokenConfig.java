package cn.dong.nexus.security.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dong.nexus.core.security.context.IAuthContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Sa-Token 配置类
 */
@Configuration
@RequiredArgsConstructor
public class SaTokenConfig implements WebMvcConfigurer {

    private final IAuthContext loginContext;

    /**
     * token 有效期
     */
    @Value("${sa-token.timeout}")
    private Long timeout;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // 注册 Sa-Token 拦截器，打开注解式鉴权功能
        registry.addInterceptor(new SaInterceptor(_ -> SaRouter.match("/**")
                .notMatch(getExcludePathPatterns())
                .check(r -> {
                    // 检查是否登录
                    StpUtil.checkLogin();
                    // 判断用户信息是否存在
                    loginContext.checkSession();
                    // 验证通过 token续期
                    StpUtil.renewTimeout(timeout);
                }))).addPathPatterns("/**");
    }

    public List<String> getExcludePathPatterns() {
        return List.of("/error",
                "/doc.html",
                "/webjars/**",
                "/swagger-resources",
                "/v3/api-docs/**",
                "/favicon.ico",
                "/sys/auth/login",
                "/sys/auth/logout",
                "/sys/config/setting"
                );
    }

}
