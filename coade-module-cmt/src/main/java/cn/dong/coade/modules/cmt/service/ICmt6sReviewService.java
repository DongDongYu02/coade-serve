package cn.dong.coade.modules.cmt.service;

import cn.dong.coade.modules.cmt.domain.dto.Cmt6sReviewDTO;
import cn.dong.coade.modules.cmt.domain.dto.Issue6sReviewRectifyDTO;
import cn.dong.coade.modules.cmt.domain.entity.Cmt6sReview;
import cn.dong.coade.modules.cmt.domain.excel.Cmt6sReviewProblemExcel;
import cn.dong.coade.modules.cmt.domain.query.Cmt6sReviewProblemQuery;
import cn.dong.coade.modules.cmt.domain.query.Cmt6sReviewQuery;
import cn.dong.coade.modules.cmt.domain.vo.Cmt6sReviewDetailVO;
import cn.dong.coade.modules.cmt.domain.vo.Cmt6sReviewProblemVO;
import cn.dong.coade.modules.cmt.domain.vo.Cmt6sReviewStatusCountVO;
import cn.dong.coade.modules.cmt.domain.vo.Cmt6sReviewVO;
import cn.dong.nexus.common.domain.vo.FileExportVO;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ICmt6sReviewService extends IService<Cmt6sReview> {
    /**
     * 新增6S评审
     */
    void create(Cmt6sReviewDTO dto);

    void reAnalyze(String reviewId);

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

    /**
     * ekp 整改完成回调
     */
    void rectifyCompleted(JSONObject result);

    /**
     * 获取问题整改项Excel数据
     */
    List<Cmt6sReviewProblemExcel> getProblemRectifyExcelData(Cmt6sReviewProblemQuery query);

    /**
     * 导出问题整改项到Excel
     */
    void exportProblemRectifyToExcel(Cmt6sReviewProblemQuery query);

    /**
     * 问题分页列表
     */
    IPage<Cmt6sReviewProblemVO> getProblemPageList(Cmt6sReviewProblemQuery query);

    /**
     * 问题导出记录
     */
    List<FileExportVO> getProblemExportList();

}
