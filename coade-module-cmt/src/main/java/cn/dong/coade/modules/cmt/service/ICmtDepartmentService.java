package cn.dong.coade.modules.cmt.service;

import cn.dong.coade.modules.cmt.domain.entity.CmtDepartment;
import cn.dong.nexus.core.base.SelectionVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ICmtDepartmentService extends IService<CmtDepartment> {

    /**
     * 从 EKP 同步部门数据
     */
    void syncFromEkp();

    /**
     * 获取部门选择列表
     *
     */
    List<SelectionVO<String, String>> getDepartmentSelection();
}
