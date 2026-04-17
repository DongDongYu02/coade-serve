package cn.dong.coade.modules.cmt.domain.excel.converter;

import cn.dong.coade.modules.cmt.constants.CmtLocalConstants;
import org.apache.fesod.sheet.converters.Converter;
import org.apache.fesod.sheet.converters.WriteConverterContext;
import org.apache.fesod.sheet.metadata.data.WriteCellData;

public class Cmt6sReviewStatusConverter implements Converter<Integer> {
    @Override
    public WriteCellData<?> convertToExcelData(WriteConverterContext<Integer> context) {
        return new WriteCellData<>(CmtLocalConstants._6S_REVIEW_STATUS.DICT_MAP.getOrDefault(context.getValue(), ""));
    }

}
