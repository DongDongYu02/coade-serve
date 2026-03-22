package cn.dong.coade.modules.cmt.domain.bo;

import lombok.Data;
import java.util.List;

@Data
public class SixSVideoResult {

    private Integer score;
    private String level;
    private String summary;
    private List<IssueItem> issues;
    private List<String> advice;

    @Data
    public static class IssueItem {
        private String type;
        private String severity;
        private String description;
        private List<String> evidenceFrames;
    }
}