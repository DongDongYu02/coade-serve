package cn.dong.nexus.modules.system.service;

import cn.dong.nexus.modules.system.domain.dto.SysDataDictDTO;
import cn.dong.nexus.modules.system.domain.dto.SysDataDictItemDTO;
import cn.dong.nexus.modules.system.domain.entity.SysDataDict;
import cn.dong.nexus.modules.system.domain.query.SysDataDictQuery;
import cn.dong.nexus.modules.system.domain.vo.SysDataDictItemVO;
import cn.dong.nexus.modules.system.domain.vo.SysDataDictVO;
import cn.dong.nexus.modules.system.domain.vo.detail.SysDataDictDetailVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ISysDataDictService extends IService<SysDataDict> {

    IPage<SysDataDictVO> getPageList(SysDataDictQuery query);

    void create(SysDataDictDTO dto);

    void update(SysDataDictDTO dto);

    void deleteById(String id);


    List<SysDataDictItemVO> getDictItems(String id);

    void createDictItem(SysDataDictItemDTO dto);

    void updateDictItem(SysDataDictItemDTO dto);

    void deleteItem(String itemId);

    SysDataDictDetailVO getDetailById(String id);
}
