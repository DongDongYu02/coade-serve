package cn.dong.nexus.core.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.MD5;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class UploadUtil {
    public static String UPLOAD_DIR;

    @Value("${coade.file-upload-path}")
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
        String extName = FileUtil.extName(file.getOriginalFilename());
        String mime = FileUtil.getMimeType(file.getOriginalFilename());
        String path = dir + "/" + fileName + "." + extName;
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
        uploadInfo.setRelativePath(today + "/" + fileName + "." + extName);
        uploadInfo.setSize(file.getSize());
        return uploadInfo;
    }

    public static UploadInfo saveFile(File file) {
        if (FileUtil.isEmpty(file)) {
            throw new RuntimeException("读取文件失败！");
        }
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String dir = UPLOAD_DIR + today;
        FileUtil.mkdir(dir);
        // 重构文件名，用md5
        String fileName = MD5.create().digestHex16(System.currentTimeMillis() + RandomUtil.randomString(6));
        String extName = FileUtil.extName(file);
        String mime = FileUtil.getMimeType(file.getPath());
        String path = dir + "/" + fileName + "." + extName;
        FileUtil.copyFile(file, FileUtil.newFile(path));
        UploadInfo uploadInfo = new UploadInfo();
        uploadInfo.setFileName(fileName);
        uploadInfo.setOriginName(FileUtil.getName(file.getPath()).split("\\.")[0]);
        uploadInfo.setMime(mime);
        uploadInfo.setAbsolutePath(path);
        uploadInfo.setRelativePath(today + "/" + fileName + "." + extName);
        uploadInfo.setSize(FileUtil.size(file));
        return uploadInfo;

    }

    public static UploadInfo saveFile(File file, String extName) {
        if (FileUtil.isEmpty(file)) {
            throw new RuntimeException("读取文件失败！");
        }
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String dir = UPLOAD_DIR + today;
        FileUtil.mkdir(dir);
        // 重构文件名，用md5
        String fileName = MD5.create().digestHex16(System.currentTimeMillis() + RandomUtil.randomString(6));
        String mime = FileUtil.getMimeType(file.getPath());
        String path = dir + "/" + fileName + "." + extName;
        FileUtil.copyFile(file, FileUtil.newFile(path));
        UploadInfo uploadInfo = new UploadInfo();
        uploadInfo.setFileName(fileName);
        uploadInfo.setOriginName(FileUtil.getName(file.getPath()).split("\\.")[0]);
        uploadInfo.setMime(mime);
        uploadInfo.setAbsolutePath(path);
        uploadInfo.setRelativePath(today + "/" + fileName + "." + extName);
        uploadInfo.setSize(FileUtil.size(file));
        return uploadInfo;
    }

    public static UploadInfo saveFile(File file, String extName, String originName) {
        if (FileUtil.isEmpty(file)) {
            throw new RuntimeException("读取文件失败！");
        }
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String dir = UPLOAD_DIR + today;
        FileUtil.mkdir(dir);
        // 重构文件名，用md5
        String fileName = MD5.create().digestHex16(System.currentTimeMillis() + RandomUtil.randomString(6));
        String mime = FileUtil.getMimeType(originName);
        String path = dir + "/" + fileName + "." + extName;
        FileUtil.copyFile(file, FileUtil.newFile(path));
        UploadInfo uploadInfo = new UploadInfo();
        uploadInfo.setFileName(fileName);
        uploadInfo.setOriginName(originName);
        uploadInfo.setMime(mime);
        uploadInfo.setAbsolutePath(path);
        uploadInfo.setRelativePath(today + "/" + fileName + "." + extName);
        uploadInfo.setSize(FileUtil.size(file));
        return uploadInfo;
    }

    /**
     * 构建一个新的MD5文件路径，返回没有带后缀的文件地址
     */
    public static String buildNewFileName() {
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String dir = UPLOAD_DIR + today;
        FileUtil.mkdir(dir);
        String fileName = MD5.create().digestHex16(System.currentTimeMillis() + RandomUtil.randomString(6));
        return dir + "/" + fileName;
    }

    /**
     * 构建一个新的MD5文件路径，返回指定格式
     */
    public static String buildNewFileName(String extName) {
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String dir = UPLOAD_DIR + today;
        FileUtil.mkdir(dir);
        String fileName = MD5.create().digestHex16(System.currentTimeMillis() + RandomUtil.randomString(6));
        return dir + "/" + fileName + "." + extName;
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
        private long size;
    }

}
