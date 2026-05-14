package cn.dong.coade.modules.cmt.support;

import cn.dong.coade.modules.cmt.domain.bo.AttendBusinessBO;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 考勤业务记录文案格式化工具。
 */
public final class AttendBizTextFormatter {

    private AttendBizTextFormatter() {
    }

    /**
     * 格式化带业务名称的文案。
     *
     * example:
     * 请假 2026-04-20 14:00 ~ 2026-04-20 17:30
     */
    public static List<String> formatBizTexts(String bizName,
                                              List<AttendBusinessBO> records,
                                              String dateTimePattern) {
        if (CollUtil.isEmpty(records)) {
            return List.of();
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateTimePattern);

        return records.stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getStartTime() != null && item.getEndTime() != null)
                .sorted(Comparator.comparing(AttendBusinessBO::getStartTime))
                .map(item -> StrUtil.format(
                        "{} {} ~ {}",
                        bizName,
                        item.getStartTime().format(formatter),
                        item.getEndTime().format(formatter)
                ))
                .collect(Collectors.toList());
    }

    /**
     * 格式化纯时间段。
     *
     * example:
     * 04-20 14:00 - 04-20 17:30
     */
    public static List<String> formatRangeTexts(List<AttendBusinessBO> records,
                                                String dateTimePattern,
                                                String separator) {
        if (CollUtil.isEmpty(records)) {
            return List.of();
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateTimePattern);

        return records.stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getStartTime() != null && item.getEndTime() != null)
                .sorted(Comparator.comparing(AttendBusinessBO::getStartTime))
                .map(item -> StrUtil.format(
                        "{}{}{}",
                        item.getStartTime().format(formatter),
                        separator,
                        item.getEndTime().format(formatter)
                ))
                .collect(Collectors.toList());
    }
}
