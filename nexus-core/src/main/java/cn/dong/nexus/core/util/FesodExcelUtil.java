package cn.dong.nexus.core.util;

import cn.dong.nexus.core.exception.BizException;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.MD5;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.fesod.sheet.FesodSheet;
import org.apache.fesod.sheet.write.style.column.LongestMatchColumnWidthStyleStrategy;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FesodExcelUtil {

    public static void setResponseProperty(HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + UUID.fastUUID() + ".xlsx");
        } catch (Exception e) {
            throw new BizException("设置导出Excel响应体失败：" + e.getMessage());
        }
    }

    public static void setResponseProperty(HttpServletResponse response, String fileName) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            fileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        } catch (Exception e) {
            throw new BizException("设置导出Excel响应体失败：" + e.getMessage());
        }
    }

    public static String generateRandomXlsxFilePath() {
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        FileUtil.mkdir(UploadUtil.UPLOAD_DIR + today);
        String fileName = MD5.create().digestHex16(System.currentTimeMillis() + RandomUtil.randomString(6));
        return today + "/" + fileName + ".xlsx";
    }

    public static <T> void write(String path, Class<T> clazz, List<T> data) {
        FesodSheet.write(UploadUtil.UPLOAD_DIR + path, clazz)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet("sheet1")
                .doWrite(data);
    }
}
