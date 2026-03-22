package cn.dong.coade.modules.cmt.domain.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EkpAttendRuleBO {

    /**
     * 每段班次：
     * 例如 {"08:00", "11:45"}
     */
    private String[][] timeRanges;

    /**
     * 出勤日：1=周一 ... 7=周日
     */
    private int[] workDays;
}
