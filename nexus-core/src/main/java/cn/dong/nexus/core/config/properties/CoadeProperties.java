package cn.dong.nexus.core.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "coade")
public class CoadeProperties {

    private Cmt cmt;
    private Ekp ekp;
    private Vrs vrs;
    private List<String> attendDeviceSn;

    @Data
    public static class Cmt {
        private String weComCorpId;
        private String weComSecret;
    }

    @Data
    public static class Ekp {
        private String serverUrl;
    }

    @Data
    public static class Vrs {
        private String appId;
        private String appSecret;
        private String allowedHost;
    }
}