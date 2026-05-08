package cn.dong.coade.modules.cmt.service.impl;

import cn.dong.coade.modules.cmt.domain.dto.CleaningAreaDTO;
import cn.dong.coade.modules.cmt.domain.entity.CmtCleaningArea;
import cn.dong.coade.modules.cmt.domain.query.CleaningAreaQuery;
import cn.dong.coade.modules.cmt.domain.vo.CleaningAreaVO;
import cn.dong.coade.modules.cmt.mapper.CmtCleaningAreaMapper;
import cn.dong.coade.modules.cmt.service.ICmtCleaningAreaService;
import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.core.security.context.IAuthContext;
import cn.dong.nexus.core.security.context.LoginUser;
import cn.dong.nexus.core.util.PageUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CmtCleaningAreaServiceImpl extends ServiceImpl<CmtCleaningAreaMapper, CmtCleaningArea> implements ICmtCleaningAreaService {
    private final IAuthContext authContext;

    @Override
    public IPage<CleaningAreaVO> getPageList(CleaningAreaQuery query) {
        Page<CmtCleaningArea> page = this.page(query.toPage(), query.toQueryWrapper());
        return PageUtil.convertPage(page, CleaningAreaVO.class);
    }

    @Override
    public void create(CleaningAreaDTO dto) {
        dto.doValidate();
        CmtCleaningArea entity = dto.toEntity();
        this.save(entity);
    }

    @Override
    public void update(CleaningAreaDTO dto) {
        dto.doValidate();
        CmtCleaningArea entity = dto.toEntity();
        this.updateById(entity);
    }

    @Override
    public List<CleaningAreaVO> getListByCurrentUser() {
        LoginUser loginUser = authContext.getLoginUser();
        boolean isAdmin = Objects.equals(loginUser.getIdentity(), GlobalConstants.UserIdentity.ADMIN);
        List<CmtCleaningArea> records = this.lambdaQuery()
                .and(!isAdmin, wrapper -> wrapper
                        .eq(CmtCleaningArea::getPrincipalUserId, loginUser.getId())
                )
                .list();
        if (records.isEmpty()) {
            return List.of();
        }
        return BeanUtil.copyToList(records, CleaningAreaVO.class);
    }


}
