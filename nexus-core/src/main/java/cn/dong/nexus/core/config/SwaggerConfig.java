package cn.dong.nexus.core.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@Configuration
public class SwaggerConfig {


    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
                .info(new Info().title("NEXUS BOOT API")
                        .description("NEXUS BOOT 接口文档")
                        .contact(getContact())
                        .version("1.0")
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")));
    }

    private Contact getContact() {
        Contact contact = new Contact();
        contact.setName("Dong");
        contact.setEmail("1025737796@qq.com");
        return contact;
    }
}
