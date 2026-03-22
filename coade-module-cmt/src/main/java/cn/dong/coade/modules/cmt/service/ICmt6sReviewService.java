package cn.dong.coade.modules.cmt.service;

import cn.dong.coade.modules.cmt.domain.dto.Cmt6sReviewDTO;
import cn.dong.coade.modules.cmt.domain.dto.Issue6sReviewRectifyDTO;
import cn.dong.coade.modules.cmt.domain.entity.Cmt6sReview;
import cn.dong.coade.modules.cmt.domain.query.Cmt6sReviewQuery;
import cn.dong.coade.modules.cmt.domain.vo.Cmt6sReviewDetailVO;
import cn.dong.coade.modules.cmt.domain.vo.Cmt6sReviewStatusCountVO;
import cn.dong.coade.modules.cmt.domain.vo.Cmt6sReviewVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ICmt6sReviewService extends IService<Cmt6sReview> {
    /**
     * 新增6S评审
     */
    void create(Cmt6sReviewDTO dto);

    /**
     * 获取分页记录
     */
    IPage<Cmt6sReviewVO> getPageList(Cmt6sReviewQuery query);

    /**
     * 获取评审详情
     */
    Cmt6sReviewDetailVO getDetailById(String id);

    /**
     * 获取 评审各状态数量统计
     */
    Cmt6sReviewStatusCountVO getStatusCount();

    /**
     * 发起问题整改
     */
    void issueRectify(Issue6sReviewRectifyDTO dto);
}
