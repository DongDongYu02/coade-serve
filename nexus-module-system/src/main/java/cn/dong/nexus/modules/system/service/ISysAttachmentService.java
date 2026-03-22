package cn.dong.nexus.modules.system.service;

import cn.dong.nexus.modules.system.domain.entity.SysAttachment;
import cn.dong.nexus.modules.system.domain.vo.AttachmentVO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

public interface ISysAttachmentService extends IService<SysAttachment> {

    /**
     * 保存附件
     */
    AttachmentVO create(MultipartFile file);
}
