package cn.dong.coade.modules.cmt.domain.bo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CmtAttendRuleBO {

    private String cmtUserId;

    private LocalDate attendDate;

    private String attendRule;
}
