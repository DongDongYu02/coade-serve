package cn.dong.coade.modules.cmt.service.impl;

import cn.dong.coade.modules.cmt.domain.entity.CmtPermission;
import cn.dong.coade.modules.cmt.mapper.CmtPermissionMapper;
import cn.dong.coade.modules.cmt.service.ICmtPermissionService;
import cn.dong.nexus.core.base.SelectionVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CmtPermissionServiceImpl extends ServiceImpl<CmtPermissionMapper, CmtPermission> implements ICmtPermissionService {

    @Override
    public List<SelectionVO<String, String>> getPermissionSelection() {
        List<CmtPermission> permissions = this.lambdaQuery()
                .select(CmtPermission::getId, CmtPermission::getName)
                .list();
        if (permissions.isEmpty()) {
            return List.of();
        }
        return permissions.stream().map(item -> new SelectionVO<>(item.getId(), item.getName())).toList();
    }


}
