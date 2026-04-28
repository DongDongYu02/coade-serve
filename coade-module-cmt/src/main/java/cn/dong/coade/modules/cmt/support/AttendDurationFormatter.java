package cn.dong.coade.modules.cmt.support;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 考勤时长格式化工具。
 */
public final class AttendDurationFormatter {

    private AttendDurationFormatter() {
    }

    /**
     * 分钟格式化为 x天x小时x分钟。
     *
     * 0 值单位不展示；总时长为 0 时返回 zeroText。
     */
    public static String formatMinutesAsDaysHoursMinutes(long totalMinutes,
                                                         long standardDayMinutes,
                                                         String zeroText) {
        if (totalMinutes <= 0) {
            return zeroText;
        }

        if (standardDayMinutes <= 0) {
            standardDayMinutes = 8 * 60L;
        }

        long days = totalMinutes / standardDayMinutes;
        long remainMinutes = totalMinutes % standardDayMinutes;

        long hours = remainMinutes / 60;
        long minutes = remainMinutes % 60;

        StringBuilder sb = new StringBuilder();

        if (days > 0) {
            sb.append(days).append("天");
        }
        if (hours > 0) {
            sb.append(hours).append("小时");
        }
        if (minutes > 0) {
            sb.append(minutes).append("分钟");
        }

        return sb.length() == 0 ? zeroText : sb.toString();
    }

    /**
     * 小时数格式化。
     *
     * example:
     * 7 -> 7小时
     * 7.5 -> 7.5小时
     */
    public static String formatHours(BigDecimal hours, String zeroText) {
        if (hours == null || hours.compareTo(BigDecimal.ZERO) <= 0) {
            return zeroText;
        }

        return hours.stripTrailingZeros().toPlainString() + "小时";
    }

    /**
     * 分钟格式化为小时文本。
     */
    public static String formatMinutesAsHours(long minutes, String zeroText) {
        if (minutes <= 0) {
            return zeroText;
        }

        return toHourNumber(minutes) + "小时";
    }

    /**
     * 分钟转小时数字文本，不带单位。
     */
    public static String toHourNumber(long minutes) {
        BigDecimal hours = BigDecimal.valueOf(minutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP)
                .stripTrailingZeros();

        return hours.toPlainString();
    }
}
