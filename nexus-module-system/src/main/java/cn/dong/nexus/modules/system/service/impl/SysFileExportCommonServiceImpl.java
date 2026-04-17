package cn.dong.nexus.modules.system.service.impl;

import cn.dong.nexus.common.api.FileExportCommonApi;
import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.common.domain.bo.FileExportBO;
import cn.dong.nexus.common.domain.vo.FileExportVO;
import cn.dong.nexus.modules.system.domain.entity.SysFileExport;
import cn.dong.nexus.modules.system.mapper.SysFileExportMapper;
import cn.dong.nexus.modules.system.service.ISysFileExportService;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysFileExportCommonServiceImpl extends ServiceImpl<SysFileExportMapper, SysFileExport> implements ISysFileExportService, FileExportCommonApi {
    @Override
    public String save(FileExportBO fileExportBO) {
        SysFileExport entity = BeanUtil.copyProperties(fileExportBO, SysFileExport.class);
        this.save(entity);
        return entity.getId();
    }

    @Override
    public void updateExportStatus(String id, Integer status) {
        this.lambdaUpdate().eq(SysFileExport::getId, id)
                .set(SysFileExport::getStatus, status)
                .update();
    }

    @Override
    public void updateExportStatus(String id, Integer status, String message) {
        this.lambdaUpdate().eq(SysFileExport::getId, id)
                .set(SysFileExport::getStatus, status)
                .set(SysFileExport::getMessage, message)
                .update();
    }

    @Override
    public List<FileExportVO> getExportList(String ownerType, String createBy) {
        List<SysFileExport> list = this.lambdaQuery()
                .eq(SysFileExport::getOwnerType, ownerType)
                .eq(SysFileExport::getCreateBy, createBy)
                .orderByDesc(SysFileExport::getCreateTime)
                .list();
        if (list.isEmpty()) {
            return List.of();
        }
        return list.stream().map(item -> {
            FileExportVO export = BeanUtil.copyProperties(item, FileExportVO.class);
            export.setCreateTime(item.getCreateTime().format(GlobalConstants.DateFormat.Y_M_D_H_M));
            return export;
        }).toList();
    }
}
