package cn.dong.nexus.common.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AttachmentOwnerType {

    CMT_6S_REVIEW("CMT_6S_REVIEW"),
    CMT_6S_REVIEW_PROBLEM("CMT_6S_REVIEW_PROBLEM"),
    CMT_6S_REVIEW_PROBLEM_RESULT("CMT_6S_REVIEW_PROBLEM_RESILT"),
    CMT_ISSUE_DEMAND("CMT_ISSUE_DEMAND"),
    CMT_ATTEND_DATA("CMT_ATTEND_DATA"),
    CMT_CLEANING_RECORD("CMT_CLEANING_RECORD");
    private final String code;

}
