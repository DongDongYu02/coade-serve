package cn.dong.coade.modules.cmt.service;

import cn.dong.coade.modules.cmt.domain.query.AttendMonthDataQuery;
import cn.dong.coade.modules.cmt.domain.vo.AttendMonthDataVO;
import cn.dong.coade.modules.cmt.support.handler.AttendMonthExcelStyleHandler;
import cn.dong.nexus.common.api.FileExportCommonApi;
import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.core.exception.BizException;
import cn.hutool.extra.spring.SpringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fesod.sheet.FesodSheet;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttendExportService {
    private final FileExportCommonApi fileExportCommonApi;

    /**
     * 固定列数量
     */
    private static final int FIXED_COL_COUNT = 11;


    @Async
    public void asyncExportMonthAttend(String exportId, String path,
                                       AttendMonthDataQuery query) {
        query.selectAllData();
        List<AttendMonthDataVO> data = SpringUtil.getBean(ICmtAttendService.class).getUserMonthAttendData(query).getRecords();
        if (data.isEmpty()) {
            throw new BizException("未查询到可导出的数据!");
        }
        log.info("开始异步导出月考勤数据，exportId={}, path={}, query={}, dataSize={}", exportId, path, query, data.size());
        try {
            YearMonth yearMonth = YearMonth.of(query.getYear(), query.getMonth());
            List<List<String>> head = this.buildHead(query.getMonth(), yearMonth.lengthOfMonth());
            List<List<Object>> excelData = this.buildData(data, yearMonth.lengthOfMonth());

            FesodSheet.write(path)
                    .inMemory(true)
                    .head(head)
                    .registerWriteHandler(new AttendMonthExcelStyleHandler(FIXED_COL_COUNT, head.size()))
                    .sheet(query.getMonth() + "月考勤")
                    .doWrite(excelData);
            fileExportCommonApi.updateExportStatus(exportId, GlobalConstants.FileExportStatus.SUCCESS);
        } catch (Exception e) {
            fileExportCommonApi.updateExportStatus(exportId, GlobalConstants.FileExportStatus.FAIL, e.getMessage());
            log.error("导出月考勤数据失败，exportId={}, path={}, query={}, dataSize={}, error={}", exportId, path, query, data == null ? 0 : data.size(), e.getMessage(), e);
        }
    }

    private List<List<String>> buildHead(Integer month, int daysOfMonth) {
        List<List<String>> head = new ArrayList<>();

        head.add(List.of("人员"));
        head.add(List.of("部门"));
        head.add(List.of("实际出勤"));
        head.add(List.of("请假"));
        head.add(List.of("加班"));
        head.add(List.of("出差"));
        head.add(List.of("迟到（次）"));
        head.add(List.of("迟到（分钟）"));
        head.add(List.of("早退（次）"));
        head.add(List.of("早退（分钟）"));
        head.add(List.of("缺卡（次）"));

        for (int day = 1; day <= daysOfMonth; day++) {
            head.add(List.of(month + "." + day));
        }
        return head;
    }

    private List<List<Object>> buildData(List<AttendMonthDataVO> list, int daysOfMonth) {
        if (list == null || list.isEmpty()) {
            return List.of();
        }

        List<List<Object>> rows = new ArrayList<>();

        for (AttendMonthDataVO vo : list) {
            List<Object> row = new ArrayList<>();

            row.add(vo.getUsername());
            row.add(vo.getDept());
            row.add(vo.getAttendDaysText());
            row.add(vo.getLeaveDaysText());
            row.add(vo.getOvertimeDuration());
            row.add(vo.getBizTripDaysText());

            row.add(vo.getLateCount());
            row.add(vo.getLateDuration());
            row.add(vo.getEarlyCount());
            row.add(vo.getEarlyDuration());
            row.add(vo.getShortages());

            Map<Integer, List<String>> dayCaseMap = Optional.ofNullable(vo.getDayCases())
                    .orElse(Collections.emptyList())
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(item -> item.getDay() != null)
                    .collect(Collectors.toMap(
                            AttendMonthDataVO.DayCase::getDay,
                            item -> Optional.ofNullable(item.getData()).orElse(Collections.emptyList()),
                            (oldList, newList) -> {
                                List<String> merged = new ArrayList<>(oldList);
                                merged.addAll(newList);
                                return merged;
                            }
                    ));

            for (int day = 1; day <= daysOfMonth; day++) {
                List<String> dayData = dayCaseMap.get(day);

                if (dayData == null || dayData.isEmpty()) {
                    row.add("无需打卡");
                } else {
                    String text = dayData.stream()
                            .filter(Objects::nonNull)
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.joining("\n"));

                    row.add(text.isEmpty() ? "无需打卡" : text);
                }
            }

            rows.add(row);
        }

        return rows;
    }
}
