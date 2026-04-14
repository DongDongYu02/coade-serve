package cn.dong.nexus.common.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AttachmentOwnerType {

    CMT_6S_REVIEW("CMT_6S_REVIEW"),
    CMT_6S_REVIEW_PROBLEM("CMT_6S_REVIEW_PROBLEM"),
    CMT_ISSUE_DEMAND("CMT_ISSUE_DEMAND");
    private final String code;

}
