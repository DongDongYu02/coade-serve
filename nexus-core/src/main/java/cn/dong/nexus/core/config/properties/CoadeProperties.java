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
    private String domain;
    private String FileUploadPath;
    private String FileAccessUrl;
    private String aesKey;

    @Data
    public static class Cmt {
        private String weComCorpId;
        private String weComSecret;
        private String weComAgentId;
        private String domain;
    }

    @Data
    public static class Ekp {
        private String serverUrl;
        private Review review;
        private String attBasePath;

        @Data
        public static class Review {
            private String leaveRequestReviewTemplateId;
            private String outgoingRequestReviewTemplateId;
            private String bizTripRequestReviewTemplateId;
            private String overtimeRequestReviewTemplateId;
            private String attendReissueReviewTemplateId;
            private String attendSpecialCaseReissueReviewTemplateId;
            private _6sField cmt6sField;
            private LeaveRequestField leaveRequestField;
            private OutgoingRequestField outgoingRequestField;
            private BizTripRequestField bizTripRequestField;
            private OvertimeRequestField overtimeRequestField;
            private AttendReissueField attendReissueField;
            private AttendSpecialCaseReissueField attendSpecialCaseReissueField;

            @Data
            public static class AttendReissueField {
                private String checkinTime;
                private String ruleCheckinTime;
                private String reason;
                private String reissueType;
            }
            @Data
            public static class AttendSpecialCaseReissueField {
                private String checkinTime;
                private String ruleCheckinTime;
                private String reason;
                private String reissueType;
            }


            @Data
            public static class _6sField {
                private String description;
                private String attKey;
                private String problemId;
            }

            @Data
            public static class LeaveRequestField {
                private String type;
                private String typeText;
                private String beginTime;
                private String endTime;
                private String duration;
                private String reason;
                private String durationFormat;
            }

            @Data
            public static class OutgoingRequestField {
                private String outDate;
                private String outTimeBegin;
                private String outTimeEnd;
                private String duration;
                private String reason;
            }

            @Data
            public static class BizTripRequestField {
                private String beginTime;
                private String endTime;
                private String duration;
                private String durationFormat;
                private String reason;
            }

            @Data
            public static class OvertimeRequestField {
                private String overtimeDate;
                private String beginTime;
                private String endTime;
                private String duration;
                private String reason;
            }
        }
    }

    @Data
    public static class Vrs {
        private String appId;
        private String appSecret;
        private String allowedHost;
    }
}