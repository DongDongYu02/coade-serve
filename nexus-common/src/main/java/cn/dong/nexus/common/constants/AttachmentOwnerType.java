package cn.dong.nexus.common.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
public enum AttachmentOwnerType {

    CMT_6S_REVIEW("CMT_6S_REVIEW"),
    CMT_6S_REVIEW_PROBLEM("CMT_6S_REVIEW_PROBLEM");

    private final String code;

    }
