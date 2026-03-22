package cn.dong.coade.modules.cmt.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "用户请假、外出、出差信息 VO")
public class UserLeaveAttendVO {

    @Schema(description = "请假时间段")
    private List<String> leaveTimes;

    @Schema(description = "外出时间段")
    private List<String> outgoingTimes;

    @Schema(description = "出差时间段")
    private List<String> businessTripTimes;
}