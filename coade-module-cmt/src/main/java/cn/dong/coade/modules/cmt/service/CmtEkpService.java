package cn.dong.coade.modules.cmt.service;

import cn.dong.coade.modules.cmt.domain.entity.CmtDepartment;
import cn.dong.coade.modules.cmt.mapper.CmtEkpMapper;
import cn.dong.nexus.common.constants.GlobalConstants;
import com.baomidou.dynamic.datasource.annotation.DS;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 查 EKP 数据服务类
 */
@Service
@RequiredArgsConstructor
@DS(GlobalConstants.DataSource.EKP_SQLSERVER)
public class CmtEkpService {
    private final CmtEkpMapper cmtEkpMapper;

    /**
     * 查询 EKP 的部门
     */
    @DS(GlobalConstants.DataSource.EKP_SQLSERVER)
    public List<CmtDepartment> getEkpDepartments() {
        return cmtEkpMapper.selectEkpDepartments();
    }
}
