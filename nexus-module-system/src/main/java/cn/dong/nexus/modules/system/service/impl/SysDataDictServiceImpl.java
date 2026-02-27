package cn.dong.nexus.modules.system.service.impl;

import cn.dong.nexus.core.api.ApiMessage;
import cn.dong.nexus.core.base.BaseEntity;
import cn.dong.nexus.core.exception.BizException;
import cn.dong.nexus.core.resmapping.ResMappingUtil;
import cn.dong.nexus.core.security.context.IAuthContext;
import cn.dong.nexus.core.util.PageUtil;
import cn.dong.nexus.modules.system.domain.dto.SysDataDictDTO;
import cn.dong.nexus.modules.system.domain.dto.SysDataDictItemDTO;
import cn.dong.nexus.modules.system.domain.entity.SysDataDict;
import cn.dong.nexus.modules.system.domain.entity.SysDataDictItem;
import cn.dong.nexus.modules.system.domain.query.SysDataDictQuery;
import cn.dong.nexus.modules.system.domain.vo.SysDataDictItemVO;
import cn.dong.nexus.modules.system.domain.vo.SysDataDictVO;
import cn.dong.nexus.modules.system.domain.vo.detail.SysDataDictDetailVO;
import cn.dong.nexus.modules.system.mapper.SysDataDictMapper;
import cn.dong.nexus.modules.system.service.ISysDataDictItemService;
import cn.dong.nexus.modules.system.service.ISysDataDictService;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SysDataDictServiceImpl extends ServiceImpl<SysDataDictMapper, SysDataDict> implements ISysDataDictService {
    private final ISysDataDictItemService dataDictItemService;

    @Override
    public IPage<SysDataDictVO> getPageList(SysDataDictQuery query) {
        IPage<SysDataDict> page = this.page(query.toPage(), query.toQueryWrapper());
        IPage<SysDataDictVO> pageVO = PageUtil.convertPage(page, SysDataDictVO.class);
        return pageVO;
    }

    @Override
    public void create(SysDataDictDTO dto) {
        dto.doValidate();
        this.save(dto.toEntity());
    }

    @Override
    public void update(SysDataDictDTO dto) {
        dto.doValidate();
        this.updateById(dto.toEntity());
    }

    @Override
    @Transactional(rollbackFor = BizException.class)
    public void deleteById(String id) {
        dataDictItemService.lambdaUpdate().eq(SysDataDictItem::getDataDictId, id).remove();
        this.removeById(id);
    }

    @Override
    public List<SysDataDictItemVO> getDictItems(String id) {
        boolean exists = this.lambdaQuery().eq(BaseEntity::getId, id).exists();
        if (!exists) {
            throw new BizException(ApiMessage.NOT_FOUND);
        }
        List<SysDataDictItem> records = dataDictItemService.lambdaQuery()
                .eq(SysDataDictItem::getDataDictId, id)
                .list();
        if (records.isEmpty()) {
            return List.of();
        }
        return BeanUtil.copyToList(records, SysDataDictItemVO.class);
    }

    @Override
    public void createDictItem(SysDataDictItemDTO dto) {
        dto.doValidate();
        dataDictItemService.save(dto.toEntity());
    }

    @Override
    public void updateDictItem(SysDataDictItemDTO dto) {
        dto.doValidate();
        dataDictItemService.updateById(dto.toEntity());
        this.lambdaUpdate().eq(BaseEntity::getId, dto.getDataDictId())
                .set(BaseEntity::getUpdateTime, new Date())
                .set(BaseEntity::getUpdateBy, SpringUtil.getBean(IAuthContext.class).getLoginUser().getId())
                .update();
    }

    @Override
    public void deleteItem(String itemId) {
        dataDictItemService.removeById(itemId);
    }

    @Override
    public SysDataDictDetailVO getDetailById(String id) {
        SysDataDict dataDict = this.getById(id);
        if (Objects.isNull(dataDict)) {
            throw new BizException(ApiMessage.NOT_FOUND);
        }
        return BeanUtil.copyProperties(dataDict, SysDataDictDetailVO.class);
    }
}
