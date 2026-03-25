package cn.dong.coade.modules.cmt.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Schema(description = "用户企微打卡记录 VO")
@NoArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class UserAttendRecordVO {

    @Schema(description = "打卡时间 yyyy-MM-dd HH:mm")
    private String checkinTime;

    @Schema(description = "应打卡时间")
    private String ruleCheckinTime;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "打卡地点")
    private String location;

    @Schema(description = "异常处理状态 null未处理  0处理中 1通过 2失败")
    private Integer exceptionStatus ;

    @Schema(description = "是否补卡")
    private Integer isReissue;

}
