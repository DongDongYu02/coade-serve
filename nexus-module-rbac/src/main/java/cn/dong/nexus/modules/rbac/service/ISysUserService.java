package cn.dong.nexus.modules.rbac.service;

import cn.dong.nexus.modules.rbac.domain.dto.SysUserDTO;
import cn.dong.nexus.modules.rbac.domain.entity.SysUser;
import cn.dong.nexus.modules.rbac.domain.query.SysUserQuery;
import cn.dong.nexus.modules.rbac.domain.vo.SysUserVO;
import cn.dong.nexus.modules.rbac.domain.vo.detail.SysUserDetailVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ISysUserService extends IService<SysUser> {
    IPage<SysUserVO> getPageList(SysUserQuery query);

    void create(SysUserDTO dto);

    void update(SysUserDTO dto);

    void deleteById(String id);

    SysUserDetailVO getDetailById(String id);

    String resetPassword(String id);
}
