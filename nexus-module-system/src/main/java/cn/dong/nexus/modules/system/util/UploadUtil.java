package cn.dong.nexus.modules.system.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.MD5;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class UploadUtil {
    public static String UPLOAD_DIR;

    @Value("${nexus.file-upload-path}")
    public void setter(String baseDir) {
        UPLOAD_DIR = baseDir;
    }

    public static UploadInfo saveMultipartFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("无法获取文件！");
        }
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String dir = UPLOAD_DIR + today;
        FileUtil.mkdir(dir);
        // 重构文件名，用md5
        String fileName = MD5.create().digestHex16(System.currentTimeMillis() + RandomUtil.randomString(6));
        String mime = FileUtil.extName(file.getOriginalFilename());
        String path = dir + "/" + fileName + "." + mime;
        try {
            file.transferTo(FileUtil.newFile(path));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        UploadInfo uploadInfo = new UploadInfo();
        uploadInfo.setFileName(fileName);
        uploadInfo.setOriginName(getFileName(file.getOriginalFilename()));
        uploadInfo.setMime(mime);
        uploadInfo.setAbsolutePath(path);
        uploadInfo.setRelativePath(today + "/" + fileName + "." + mime);

        return uploadInfo;
    }


    public static String getFileName(String path) {
        if (StrUtil.isBlank(path)) {
            return StrUtil.EMPTY;
        }
        return path.substring(0, path.lastIndexOf("."));
    }

    public static String modifyExtName(String path, String extName) {
        if (StrUtil.isBlank(path)) {
            return StrUtil.EMPTY;
        }
        String fileName = getFileName(path);
        return fileName + "." + extName;
    }

    @Data
    public static class UploadInfo {
        private String originName;
        private String fileName;
        private String relativePath;
        private String absolutePath;
        private String mime;
    }

}
