package cn.dong.coade.modules.cmt.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum AttendRuleType {
    /**
     * 固定上下班
     */
    FIXED,
    /**
     * 注塑部规则
     */
    IMD(new String[][]{{"08:00", "08:00"}, {"11:15", "11:45"}, {"20:30", "20:30"}}),
    /**
     * 无需打卡
     */
    EMPTY;

    private String[][] rule;


}
