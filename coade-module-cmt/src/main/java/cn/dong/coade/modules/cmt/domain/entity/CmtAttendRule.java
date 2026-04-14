package cn.dong.coade.modules.cmt.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("cmt_attend_rule")
public class CmtAttendRule {

    /**
     * ID
     */
    private String id;

    /**
     * 用户id
     */
    private String weComId;

    /**
     * 考勤日期
     */
    private LocalDate attendDate;

    /**
     * 规则
     */
    private String rule;

}
