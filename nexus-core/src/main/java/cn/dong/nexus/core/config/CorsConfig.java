package cn.dong.nexus.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {
    /**
     * 跨域支持
     **/
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.addAllowedOrigin("*"); // 允许所有来源
        corsConfig.addAllowedMethod("*"); // 允许所有 HTTP 方法
        corsConfig.addAllowedHeader("*"); // 允许所有 HTTP 标头
        corsConfig.setAllowCredentials(false); // 不允许携带身份信息（如Cookies）

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsFilter(source);
    }

}
