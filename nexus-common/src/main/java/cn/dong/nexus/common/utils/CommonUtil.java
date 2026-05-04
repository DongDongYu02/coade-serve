package cn.dong.nexus.common.utils;

import java.time.Duration;

public class CommonUtil {

    public static String formatDuration(Duration duration) {
        if (duration == null || duration.isNegative() || duration.isZero()) {
            return "0秒";
        }

        long seconds = duration.getSeconds();

        long days = seconds / (24 * 60 * 60);
        seconds %= 24 * 60 * 60;

        long hours = seconds / (60 * 60);
        seconds %= 60 * 60;

        long minutes = seconds / 60;
        seconds %= 60;

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
        if (seconds > 0) {
            sb.append(seconds).append("秒");
        }

        return sb.isEmpty() ? "0秒" : sb.toString();
    }
}
