package cn.dong.coade.modules.cmt.support.handler;

import org.apache.fesod.sheet.metadata.Head;
import org.apache.fesod.sheet.metadata.data.WriteCellData;
import org.apache.fesod.sheet.write.handler.CellWriteHandler;
import org.apache.fesod.sheet.write.handler.RowWriteHandler;
import org.apache.fesod.sheet.write.handler.SheetWriteHandler;
import org.apache.fesod.sheet.write.metadata.holder.WriteSheetHolder;
import org.apache.fesod.sheet.write.metadata.holder.WriteTableHolder;
import org.apache.fesod.sheet.write.metadata.holder.WriteWorkbookHolder;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.*;

import java.util.List;

public class AttendMonthExcelStyleHandler implements CellWriteHandler, RowWriteHandler, SheetWriteHandler {

    /**
     * 日期列开始下标
     */
    private final int dayStartColIndex;

    /**
     * 总列数
     */
    private final int totalColCount;

    private CellStyle headStyle;
    private CellStyle bodyStyle;
    private CellStyle dayCellStyle;

    private XSSFFont defaultFont;
    private XSSFFont leaveFont;
    private XSSFFont outFont;
    private XSSFFont bizTripFont;
    private XSSFFont overtimeFont;
    private XSSFFont normalFont;
    private XSSFFont abnormalFont;
    private XSSFFont restFont;

    public AttendMonthExcelStyleHandler(int fixedColCount, int totalColCount) {
        this.dayStartColIndex = fixedColCount;
        this.totalColCount = totalColCount;
    }

    @Override
    public void afterSheetCreate(
            WriteWorkbookHolder writeWorkbookHolder,
            WriteSheetHolder writeSheetHolder
    ) {
        Sheet sheet = writeSheetHolder.getSheet();

        // 冻结首行和前两列
        sheet.createFreezePane(2, 1);

        // 表头筛选
        if (totalColCount > 0) {
            sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, totalColCount - 1));
        }

        // 固定列宽
        sheet.setColumnWidth(0, 14 * 256);  // 人员
        sheet.setColumnWidth(1, 16 * 256);  // 部门
        sheet.setColumnWidth(2, 14 * 256);  // 实际出勤
        sheet.setColumnWidth(3, 14 * 256);  // 请假
        sheet.setColumnWidth(4, 12 * 256);  // 加班
        sheet.setColumnWidth(5, 12 * 256);  // 出差
        sheet.setColumnWidth(6, 12 * 256);  // 迟到次
        sheet.setColumnWidth(7, 14 * 256);  // 迟到分钟
        sheet.setColumnWidth(8, 12 * 256);  // 早退次
        sheet.setColumnWidth(9, 14 * 256);  // 早退分钟
        sheet.setColumnWidth(10, 12 * 256); // 缺卡次

        // 日期列宽
        for (int i = dayStartColIndex; i < totalColCount; i++) {
            sheet.setColumnWidth(i, 30 * 256);
        }
    }

    @Override
    public void afterCellDispose(
            WriteSheetHolder writeSheetHolder,
            WriteTableHolder writeTableHolder,
            List<WriteCellData<?>> cellDataList,
            Cell cell,
            Head head,
            Integer relativeRowIndex,
            Boolean isHead
    ) {
        Workbook workbook = cell.getSheet().getWorkbook();
        initStyle(workbook);

        if (Boolean.TRUE.equals(isHead)) {
            cell.setCellStyle(headStyle);
            return;
        }

        int colIndex = cell.getColumnIndex();

        // 汇总列
        if (colIndex < dayStartColIndex) {
            cell.setCellStyle(bodyStyle);
            return;
        }

        // 日期列
        cell.setCellStyle(dayCellStyle);

        String text = getCellText(cell);
        if (text == null || text.isBlank()) {
            return;
        }

        setRichTextByLine(cell, text);
    }

    @Override
    public void afterRowDispose(
            WriteSheetHolder writeSheetHolder,
            WriteTableHolder writeTableHolder,
            Row row,
            Integer relativeRowIndex,
            Boolean isHead
    ) {
        if (Boolean.TRUE.equals(isHead)) {
            row.setHeightInPoints(30F);
            return;
        }

        int maxLine = 1;

        short lastCellNum = row.getLastCellNum();
        if (lastCellNum <= 0) {
            row.setHeightInPoints(48F);
            return;
        }

        for (int i = dayStartColIndex; i < lastCellNum; i++) {
            Cell cell = row.getCell(i);
            if (cell == null) {
                continue;
            }

            String text = getCellText(cell);
            if (text == null || text.isBlank()) {
                continue;
            }

            int lineCount = text.split("\n", -1).length;
            maxLine = Math.max(maxLine, lineCount);
        }

        float height = Math.max(70F, Math.min(220F, maxLine * 22F + 24F));
        row.setHeightInPoints(height);
    }

    /**
     * 按每一行内容设置不同字体颜色
     */
    private void setRichTextByLine(Cell cell, String text) {
        XSSFRichTextString richTextString = new XSSFRichTextString(text);

        String[] lines = text.split("\n", -1);
        int startIndex = 0;

        for (String line : lines) {
            int endIndex = startIndex + line.length();

            XSSFFont font = getFontByLine(line);

            if (endIndex > startIndex) {
                richTextString.applyFont(startIndex, endIndex, font);
            }

            // 跳过换行符 \n
            startIndex = endIndex + 1;
        }

        cell.setCellValue(richTextString);
    }

    /**
     * 根据每一行内容判断字体颜色
     */
    private XSSFFont getFontByLine(String line) {
        System.out.println("当前行内容：" + line);
        if (line == null || line.isBlank()) {
            return defaultFont;
        }

        // 缺卡、迟到、早退：红色
        if (line.contains("缺卡")
                || line.contains("迟到")
                || line.contains("早退")) {
            return abnormalFont;
        }

        // 请假：橙色
        if (line.contains("请假")) {
            return leaveFont;
        }

        // 外出：青色
        if (line.contains("外出")) {
            return outFont;
        }

        // 出差：蓝色
        if (line.contains("出差")) {
            return bizTripFont;
        }

        // 加班：紫色
        if (line.contains("加班")) {
            return overtimeFont;
        }

        // 正常：绿色
        if (line.contains("正常")) {
            return normalFont;
        }

        // 无需打卡、待打卡、未开始：灰色
        if (line.contains("无需打卡")
                || line.contains("待打卡")
                || line.contains("未开始")) {
            return restFont;
        }

        return defaultFont;
    }

    /**
     * 初始化样式，只初始化一次
     */
    private void initStyle(Workbook workbook) {
        if (headStyle != null) {
            return;
        }

        XSSFWorkbook xssfWorkbook = getXSSFWorkbook(workbook);

        // ========== 字体 ==========
        defaultFont = createXSSFFont(xssfWorkbook, "#262626");

        leaveFont = createXSSFFont(xssfWorkbook, "#fa8c16");     // 请假：橙色
        outFont = createXSSFFont(xssfWorkbook, "#13c2c2");       // 外出：青色
        bizTripFont = createXSSFFont(xssfWorkbook, "#1677ff");   // 出差：蓝色
        overtimeFont = createXSSFFont(xssfWorkbook, "#722ed1");  // 加班：紫色
        normalFont = createXSSFFont(xssfWorkbook, "#16a34a");    // 正常：绿色
        abnormalFont = createXSSFFont(xssfWorkbook, "#ff4d4f");  // 缺卡、迟到、早退：红色
        restFont = createXSSFFont(xssfWorkbook, "#8c8c8c");      // 无需打卡、待打卡、未开始：灰色

        // ========== 表头样式 ==========
        headStyle = workbook.createCellStyle();
        headStyle.setAlignment(HorizontalAlignment.CENTER);
        headStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headStyle.setWrapText(true);
        setBorder(headStyle);

        XSSFFont headFont = createXSSFFont(xssfWorkbook, "#262626");
        headFont.setBold(true);
        headStyle.setFont(headFont);

        // ========== 汇总列样式 ==========
        bodyStyle = workbook.createCellStyle();
        bodyStyle.setAlignment(HorizontalAlignment.CENTER);
        bodyStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        bodyStyle.setWrapText(true);
        bodyStyle.setFont(defaultFont);
        setBorder(bodyStyle);

        // ========== 日期列样式 ==========
        dayCellStyle = workbook.createCellStyle();
        dayCellStyle.setAlignment(HorizontalAlignment.CENTER);
        dayCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        dayCellStyle.setWrapText(true);
        dayCellStyle.setFont(defaultFont);
        setBorder(dayCellStyle);
    }

    /**
     * 兼容 SXSSFWorkbook / XSSFWorkbook
     */
    private XSSFWorkbook getXSSFWorkbook(Workbook workbook) {
        if (workbook instanceof SXSSFWorkbook sxssfWorkbook) {
            return sxssfWorkbook.getXSSFWorkbook();
        }

        if (workbook instanceof XSSFWorkbook xssfWorkbook) {
            return xssfWorkbook;
        }

        throw new IllegalStateException("当前导出只支持 xlsx，workbook 类型：" + workbook.getClass().getName());
    }

    /**
     * 创建 XSSF 自定义颜色字体
     */
    private XSSFFont createXSSFFont(XSSFWorkbook workbook, String hexColor) {
        XSSFFont font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setFontName("微软雅黑");

        if (hexColor != null && !hexColor.isBlank()) {
            java.awt.Color awtColor = java.awt.Color.decode(hexColor);
            XSSFColor xssfColor = new XSSFColor(awtColor, new DefaultIndexedColorMap());
            font.setColor(xssfColor);
        }

        return font;
    }

    /**
     * 设置边框
     */
    private void setBorder(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        style.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
    }

    /**
     * 获取单元格文本
     */
    private String getCellText(Cell cell) {
        if (cell == null) {
            return "";
        }

        try {
            CellType cellType = cell.getCellType();

            if (cellType == CellType.STRING) {
                return cell.getStringCellValue();
            }

            if (cellType == CellType.NUMERIC) {
                double value = cell.getNumericCellValue();

                if (value == Math.floor(value)) {
                    return String.valueOf((long) value);
                }

                return String.valueOf(value);
            }

            if (cellType == CellType.BOOLEAN) {
                return String.valueOf(cell.getBooleanCellValue());
            }

            if (cellType == CellType.FORMULA) {
                return cell.getCellFormula();
            }

            return "";
        } catch (Exception e) {
            return "";
        }
    }
}