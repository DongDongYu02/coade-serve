package cn.dong.coade.modules.cmt.service;

import cn.dong.coade.modules.cmt.domain.dto.IssueDemandAssessmentedDTO;
import cn.dong.coade.modules.cmt.domain.dto.IssueDemandCompletedDTO;
import cn.dong.coade.modules.cmt.domain.dto.IssueDemandDTO;
import cn.dong.coade.modules.cmt.domain.dto.IssueDemandRejectDTO;
import cn.dong.coade.modules.cmt.domain.entity.CmtIssueDemand;
import cn.dong.coade.modules.cmt.domain.query.IssueDemandQuery;
import cn.dong.coade.modules.cmt.domain.vo.IssueDemandDetailVO;
import cn.dong.coade.modules.cmt.domain.vo.IssueDemandVO;
import cn.dong.nexus.core.base.SelectionVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ICmtIssueDemandService extends IService<CmtIssueDemand> {

    void create(IssueDemandDTO dto);

    IPage<IssueDemandVO> getPageList(IssueDemandQuery query);

    List<IssueDemandVO> getList(IssueDemandQuery query);

    IssueDemandDetailVO getDetailById(String id);

    void edit(IssueDemandDTO dto);

    /**
     * 指派负责人
     *
     * @param principalUserId 负责人用户ID
     */
    void assigned(String id, String principalUserId);

    /**
     * 完成评估
     */
    void assessmented(IssueDemandAssessmentedDTO id);

    /**
     * 驳回
     */
    void reject(IssueDemandRejectDTO dto);

    /**
     * 处理完成
     */
    void completed(IssueDemandCompletedDTO dto);

    /**
     * 获取负责人列表
     */
    List<SelectionVO<String,String>> getPrincipalSelection();

}
