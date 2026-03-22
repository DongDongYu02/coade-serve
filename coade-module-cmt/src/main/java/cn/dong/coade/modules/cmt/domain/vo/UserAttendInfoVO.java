package cn.dong.coade.modules.cmt.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Schema(description = "用户企微考勤信息 VO")
@AllArgsConstructor
@NoArgsConstructor
public class UserAttendInfoVO {

    @Schema(description = "打卡规则")
    private String rule;

    @Schema(description = "用户打卡记录")
    private List<UserAttendRecordVO> userAttendRecord;

    @Schema(description = "用户考勤业务记录 请假、外出、出差")
    private UserLeaveAttendVO userLeaveAttend;


}
