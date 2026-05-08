package cn.dong.coade.modules.cmt.service;

import cn.dong.coade.modules.cmt.domain.dto.CleaningRecordDTO;
import cn.dong.coade.modules.cmt.domain.entity.CmtCleaningRecord;
import cn.dong.coade.modules.cmt.domain.query.CleaningRecordQuery;
import cn.dong.coade.modules.cmt.domain.vo.CleaningRecordVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ICmtCleaningRecordService extends IService<CmtCleaningRecord> {
    /**
     * 分页列表
     */
    IPage<CleaningRecordVO> getPageList(CleaningRecordQuery query);

    /**
     * 新增
     */
    void create(CleaningRecordDTO dto);

    /**
     * 获取近一个月的打扫记录
     */
    List<CleaningRecordVO> getList(CleaningRecordQuery query);
}
