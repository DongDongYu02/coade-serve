package cn.dong.coade.modules.cmt.domain.excel;

import cn.dong.coade.modules.cmt.domain.excel.converter.Cmt6sReviewStatusConverter;
import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.core.resmapping.annotation.ResMapping;
import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnore;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.apache.fesod.sheet.annotation.write.style.*;
import org.apache.fesod.sheet.enums.poi.FillPatternTypeEnum;
import org.apache.fesod.sheet.enums.poi.VerticalAlignmentEnum;

import java.io.File;
import java.time.LocalDateTime;

@Data
@HeadStyle(fillPatternType = FillPatternTypeEnum.SOLID_FOREGROUND, fillForegroundColor = 9)
@HeadFontStyle(fontHeightInPoints = 12)
@ContentRowHeight(100)
@HeadRowHeight(30)
@ContentStyle(verticalAlignment = VerticalAlignmentEnum.CENTER)
public class Cmt6sReviewProblemExcel {

    @ExcelProperty("部门")
    private String deptName;

    @ExcelProperty("整改标题")
    private String title;

    @ExcelProperty("问题描述")
    private String description;

    @ExcelProperty("问题照片")
    @ColumnWidth(30)
    private File problemImage;

    @ExcelProperty("整改结果")
    private File rectifyImage;

    @ExcelProperty("负责人")
    private String responsiblePersonName;

    @ExcelProperty("协助人")
    private String assisterName;

    @ExcelProperty(value = "状态", converter = Cmt6sReviewStatusConverter.class)
    private Integer status;

    @ExcelIgnore
    private LocalDateTime createTime;

    @ExcelIgnore
    private String id;

    @ResMapping(sourceTable = GlobalConstants.TableName.CMT_6S_REVIEW,
            values = {"deptId", "responsiblePersonId", "title", "status"},
            targets = {"deptId", "responsiblePersonId", "title", "status"})
    @ExcelIgnore
    private String reviewId;

    @ResMapping(sourceTable = GlobalConstants.TableName.CMT_DEPT)
    @ExcelIgnore
    private String deptId;

    @ResMapping(sourceTable = GlobalConstants.TableName.CMT_USER, key = "ekpId", values = "username")
    @ExcelIgnore
    private String responsiblePersonId;

    @ResMapping(sourceTable = GlobalConstants.TableName.CMT_USER, key = "ekpId", values = "username", targets = "assisterName")
    @ExcelIgnore
    private String assister;


}
