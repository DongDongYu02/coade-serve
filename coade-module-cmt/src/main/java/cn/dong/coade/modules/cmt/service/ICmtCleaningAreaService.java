package cn.dong.coade.modules.cmt.service;

import cn.dong.coade.modules.cmt.domain.dto.CleaningAreaDTO;
import cn.dong.coade.modules.cmt.domain.entity.CmtCleaningArea;
import cn.dong.coade.modules.cmt.domain.query.CleaningAreaQuery;
import cn.dong.coade.modules.cmt.domain.vo.CleaningAreaVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ICmtCleaningAreaService extends IService<CmtCleaningArea> {
    /**
     * 分页累表
     */
    IPage<CleaningAreaVO> getPageList(CleaningAreaQuery query);

    /**
     * 新增
     */
    void create(CleaningAreaDTO dto);

    /**
     * 编辑
     */
    void update(CleaningAreaDTO dto);


    /**
     * 获取当前保洁的负责区域列表
     */
    List<CleaningAreaVO> getListByCurrentUser();
}
