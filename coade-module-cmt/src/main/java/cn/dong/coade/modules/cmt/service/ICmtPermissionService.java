package cn.dong.coade.modules.cmt.service;

import cn.dong.coade.modules.cmt.domain.entity.CmtPermission;
import cn.dong.nexus.core.base.SelectionVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ICmtPermissionService extends IService<CmtPermission> {

    /**
     * 获取权限选择列表
     */
    List<SelectionVO<String, String>> getPermissionSelection();

}
