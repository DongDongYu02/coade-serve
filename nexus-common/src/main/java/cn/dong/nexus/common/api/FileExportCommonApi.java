package cn.dong.nexus.common.api;

import cn.dong.nexus.common.domain.bo.FileExportBO;
import cn.dong.nexus.common.domain.vo.FileExportVO;

import java.util.List;

public interface FileExportCommonApi {

    /**
     * 创建导出记录
     *
     * @return id
     */
    String save(FileExportBO fileExportBO);

    /**
     * 更新导出状态
     */
    void updateExportStatus(String id, Integer status);

    void updateExportStatus(String id, Integer status, String message);

    List<FileExportVO> getExportList(String ownerType, String createBy);


}
