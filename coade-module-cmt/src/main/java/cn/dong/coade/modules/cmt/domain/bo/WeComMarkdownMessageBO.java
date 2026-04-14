package cn.dong.coade.modules.cmt.domain.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeComMarkdownMessageBO {

    private String touser;

    private String msgtype = "textcard";

    private String agentid;

    private Content textcard;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Content {
        private String title;
        private String description;
        private String url;
        private String btntxt;
    }
}
