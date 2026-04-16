package cn.dong.coade.modules.cmt.service;

import cn.dong.coade.modules.cmt.domain.bo.EkpAttachmentBO;
import cn.dong.coade.modules.cmt.domain.entity.CmtDepartment;
import cn.dong.coade.modules.cmt.mapper.CmtEkpMapper;
import cn.dong.nexus.common.constants.ApiConstants;
import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.core.config.properties.CoadeProperties;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.http.HttpUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 查 EKP 数据服务类
 */
@Service
@RequiredArgsConstructor
@DS(GlobalConstants.DataSource.EKP_SQLSERVER)
public class CmtEkpService {
    private final CmtEkpMapper cmtEkpMapper;
    private final CoadeProperties coadeProperties;

    /**
     * 查询 EKP 的部门
     */
    @DS(GlobalConstants.DataSource.EKP_SQLSERVER)
    public List<CmtDepartment> getEkpDepartments() {
        return cmtEkpMapper.selectEkpDepartments();
    }


    @DS(GlobalConstants.DataSource.EKP_SQLSERVER)
    public Map<String, List<String>> getAttachmentsKeyMap(List<String> keys) {
        List<EkpAttachmentBO> attachments = cmtEkpMapper.selectAttachmentsByKeys(keys);
        if (attachments.isEmpty()) {
            return Map.of();
        }

        return attachments.stream()
                // 组装文件名
                .peek(item -> item.setAttPath(item.getAttKey() + FileUtil.extName(item.getAttName())))
                // 根据attKey分组
                .collect(Collectors.groupingBy(
                        EkpAttachmentBO::getAttKey,
                        Collectors.mapping(EkpAttachmentBO::getAttPath, Collectors.toList())));
    }


    @DS(GlobalConstants.DataSource.EKP_SQLSERVER)
    public Map<String, String> getAttachmentKeyMap(List<String> keys) {
        List<EkpAttachmentBO> attachments = cmtEkpMapper.selectAttachmentsByKeys(keys);
        if (attachments.isEmpty()) {
            return Map.of();
        }

        return attachments.stream()
                // 组装文件名
                .peek(item -> item.setAttPath(item.getAttKey() + "." + FileUtil.extName(item.getAttName())))
                // 根据attKey分组
                .collect(Collectors.toMap(EkpAttachmentBO::getAttKey, EkpAttachmentBO::getAttPath));
    }

    @DS(GlobalConstants.DataSource.EKP_SQLSERVER)
    public List<EkpAttachmentBO> getAttachmentsByKeys(List<String> keys) {
        List<EkpAttachmentBO> ekpAttachments = cmtEkpMapper.selectAttachmentsByKeys(keys);
        if (ekpAttachments.isEmpty()) {
            return List.of();
        }
        ekpAttachments.forEach(item -> item.setAttExtName(FileUtil.extName(item.getAttName())));
        return ekpAttachments;
    }


    public File getAttachmentFile(String attId, File file) {
        String url = UrlBuilder
                .of(coadeProperties.getEkp().getServerUrl() + ApiConstants.EKP_DOWNLOAD_FILE)
                .addQuery("fdId", attId)
                .build();
        String downloadUrl = HttpUtil.get(url);
        return HttpUtil.downloadFileFromUrl(downloadUrl, file);
    }
}
