package cn.dong.nexus.modules.system.controller;

import cn.dong.nexus.core.api.Result;
import cn.dong.nexus.modules.system.domain.vo.AttachmentVO;
import cn.dong.nexus.modules.system.util.UploadUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/sys/file")
@Tag(name = "附件管理")
@RequiredArgsConstructor
public class FileController {
    @Value("${nexus.file-access-url}")
    private String fileAccessUrl;


    @PostMapping("/upload")
    @Operation(summary = "文件上传")
    public Result<AttachmentVO> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("未获取到上传的文件！");
        }
        UploadUtil.UploadInfo uploadInfo = UploadUtil.saveMultipartFile(file);
        AttachmentVO vo = new AttachmentVO();
        vo.setMime(uploadInfo.getMime());
        vo.setName(uploadInfo.getOriginName());
        vo.setPath(uploadInfo.getRelativePath());
        vo.setUrl(fileAccessUrl + uploadInfo.getRelativePath());
        return Result.success(vo);
    }

}
