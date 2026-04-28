package cn.dong.coade.modules.cmt.domain.vo;

import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.core.resmapping.annotation.ResMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "月考勤数据 VO")
public class AttendMonthDataVO {

    @Schema(description = "用户ID")
    @ResMapping(sourceTable = GlobalConstants.TableName.CMT_USER, values = "dept", targets = "dept")
    private String userId;

    @Schema(description = "部门")
    private String dept;

    @Schema(description = "企微ID")
    private String weComId;

    @Schema(description = "出勤天数")
    private BigDecimal attendDays;

    @Schema(description = "出勤天数格式化文本")
    private String attendDaysText;

    @Schema(description = "请假天数")
    private BigDecimal leaveDays;

    @Schema(description = "请假天数格式化文本")
    private String leaveDaysText;

    @Schema(description = "当天考勤情况")
    private List<DayCase> dayCases;

    @Schema(description = "加班时长")
    private String overtimeDuration;

    @Schema(description = "出差天数")
    private BigDecimal bizTripDays;

    @Schema(description = "出差天数格式化文本")
    private String bizTripDaysText;

    @Schema(description = "缺卡次数")
    private Integer shortages;

    @Schema(description = "迟到次数")
    private Integer lateCount;

    @Schema(description = "迟到时长 分钟")
    private Integer lateDuration;

    @Schema(description = "早退次数")
    private Integer earlyCount;

    @Schema(description = "早退时长 分钟")
    private Integer earlyDuration;


    @Data
    public static class DayCase {
        @Schema(description = "当月第几天")
        private Integer day;

        /**
         * example:
         * [
         * '请假 2026-04-20 14:00 ~ 2026-04-20 17:30',
         * '07:30 正常',
         * '11:34 正常',
         * '12:24 正常',
         * '14:00 下班缺卡'
         * ]
         */
        @Schema(description = "当天考勤数据")
        private List<String> data;

    }
}
