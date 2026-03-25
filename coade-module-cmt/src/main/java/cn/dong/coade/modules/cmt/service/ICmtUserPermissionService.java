package cn.dong.coade.modules.cmt.service;

import cn.dong.coade.modules.cmt.domain.entity.CmtUserPermission;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ICmtUserPermissionService extends IService<CmtUserPermission> {

    /**
     * 授予CMT用户标准权限
     */
    void grantBasicPermission(List<String> cmtUserIds);


    List<String> getPermissionsByUserId(String userId,Integer userIdentity);

    void removeBasicPermission(List<String> cmtUserIds);
}
