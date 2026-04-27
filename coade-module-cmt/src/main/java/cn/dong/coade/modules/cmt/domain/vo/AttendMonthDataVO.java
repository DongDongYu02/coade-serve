package cn.dong.coade.modules.cmt.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "月考勤数据 VO")
public class AttendMonthDataVO {

    @Schema(description = "用户ID")
    private String userId;

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
