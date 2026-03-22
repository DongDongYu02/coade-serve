package cn.dong.coade.modules.cmt.service.impl;

import cn.dong.coade.modules.cmt.constants.CmtLocalConstants;
import cn.dong.coade.modules.cmt.domain.dto.Cmt6sReviewDTO;
import cn.dong.coade.modules.cmt.domain.dto.Issue6sReviewRectifyDTO;
import cn.dong.coade.modules.cmt.domain.entity.Cmt6sReview;
import cn.dong.coade.modules.cmt.domain.entity.Cmt6sReviewProblem;
import cn.dong.coade.modules.cmt.domain.query.Cmt6sReviewQuery;
import cn.dong.coade.modules.cmt.domain.vo.Cmt6sReviewDetailVO;
import cn.dong.coade.modules.cmt.domain.vo.Cmt6sReviewStatusCountVO;
import cn.dong.coade.modules.cmt.domain.vo.Cmt6sReviewVO;
import cn.dong.coade.modules.cmt.mapper.Cmt6sReviewMapper;
import cn.dong.coade.modules.cmt.service.AI6sService;
import cn.dong.coade.modules.cmt.service.ICmt6sReviewProblemService;
import cn.dong.coade.modules.cmt.service.ICmt6sReviewService;
import cn.dong.nexus.common.api.CommonAttachmentService;
import cn.dong.nexus.common.constants.AttachmentOwnerType;
import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.common.domain.bo.AttachmentBO;
import cn.dong.nexus.common.domain.bo.AttachmentOwnerSaveBO;
import cn.dong.nexus.common.domain.vo.AttachmentVO;
import cn.dong.nexus.core.api.ApiMessage;
import cn.dong.nexus.core.exception.BizException;
import cn.dong.nexus.core.resmapping.ResMappingUtil;
import cn.dong.nexus.core.util.PageUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class Cmt6sReviewServiceImpl extends ServiceImpl<Cmt6sReviewMapper, Cmt6sReview> implements ICmt6sReviewService {

    private final CommonAttachmentService attachmentService;
    private final ICmt6sReviewProblemService cmt6sReviewProblemService;
    private final AI6sService ai6sService;

    @Value("${nexus.file-access-url}")
    private String fileAccessUrl;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Cmt6sReviewDTO dto) {
        dto.doValidate();
        Cmt6sReview entity = dto.toEntity();
        this.save(entity);
        // 关联附件
        attachmentService.saveAttachmentsOwner(dto.getAttachmentIds(), AttachmentOwnerType.CMT_6S_REVIEW, entity.getId());
        // 调用 AI6S 分析
        ai6sService.analyze(entity.getId(), dto.getAttachmentIds());
    }

    @Override
    public IPage<Cmt6sReviewVO> getPageList(Cmt6sReviewQuery query) {
        Page<Cmt6sReview> page = this.page(query.toPage(), query.toQueryWrapper());
        return PageUtil.convertPage(page, Cmt6sReviewVO.class);
    }

    @Override
    public Cmt6sReviewDetailVO getDetailById(String id) {
        Cmt6sReview record = this.getById(id);
        if (Objects.isNull(record)) {
            throw new BizException(ApiMessage.NOT_FOUND);
        }
        Cmt6sReviewDetailVO detail = BeanUtil.copyProperties(record, Cmt6sReviewDetailVO.class);
        // 获取评审素材
        List<AttachmentBO> materials = attachmentService.getByOwners(AttachmentOwnerType.CMT_6S_REVIEW, List.of(id));
        detail.setMaterials(materials.stream().map(item -> new AttachmentVO(item.getId(), fileAccessUrl + item.getPath())).toList());

        // 获取评审问题
        List<Cmt6sReviewProblem> dbProblems = cmt6sReviewProblemService.lambdaQuery().eq(Cmt6sReviewProblem::getReviewId, id).list();
        // 没有问题
        if (dbProblems.isEmpty()) {
            detail.setProblems(List.of());
            return detail;
        }
        List<String> problemIds = dbProblems.stream().map(Cmt6sReviewProblem::getId).toList();
        // 查询问题图片
        List<AttachmentBO> problemImages = attachmentService.getByOwners(AttachmentOwnerType.CMT_6S_REVIEW_PROBLEM, problemIds);
        // 根据问题 id 分组
        Map<String, List<AttachmentBO>> imageGroup = problemImages.stream()
                .collect(Collectors.groupingBy(AttachmentBO::getOwnerId));
        List<Cmt6sReviewDetailVO.Problem> problems = BeanUtil.copyToList(dbProblems, Cmt6sReviewDetailVO.Problem.class);
        problems.forEach(item -> {
            List<AttachmentBO> images = imageGroup.getOrDefault(item.getId(), List.of());
            item.setImages(images.stream().map(img -> new AttachmentVO(img.getId(), fileAccessUrl + img.getPath())).collect(Collectors.toList()));
        });
        detail.setProblems(problems);
        // 字段翻译
        ResMappingUtil.translateObjField(detail);
        ResMappingUtil.translateField(detail.getProblems());
        return detail;
    }

    @Override
    public Cmt6sReviewStatusCountVO getStatusCount() {
        Cmt6sReviewStatusCountVO vo = new Cmt6sReviewStatusCountVO(0L, 0L, 0L);
        vo.setTotal(this.count());
        List<Cmt6sReview> reviews = this.lambdaQuery().select(Cmt6sReview::getStatus)
                .in(Cmt6sReview::getStatus, CmtLocalConstants._6S_REVIEW_STATUS.PENDING_RECTIFY, CmtLocalConstants._6S_REVIEW_STATUS.COMPLETED)
                .list();
        if (reviews.isEmpty()) return vo;
        long pendingRectify = reviews.stream().filter(item -> CmtLocalConstants._6S_REVIEW_STATUS.PENDING_RECTIFY.equals(item.getStatus())).count();
        long rectifyCompleted = reviews.stream().filter(item -> CmtLocalConstants._6S_REVIEW_STATUS.COMPLETED.equals(item.getStatus())).count();
        vo.setPendingRectify(pendingRectify);
        vo.setRectifyCompleted(rectifyCompleted);
        return vo;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void issueRectify(Issue6sReviewRectifyDTO dto) {
        Cmt6sReview review = this.getById(dto.getId());
        if (Objects.isNull(review)) {
            throw new BizException(ApiMessage.NOT_FOUND);
        }
        List<Issue6sReviewRectifyDTO.Problem> problems = dto.getProblems();
        // 需要更新的问题
        List<Issue6sReviewRectifyDTO.Problem> updateProblems = problems.stream().filter(item -> Issue6sReviewRectifyDTO.PROBLEM_CTRL_UPDATE.equals(item.getCtrl())).toList();
        // 需要删除的问题 ID
        List<String> removeProblemIds = problems.stream().filter(item -> Issue6sReviewRectifyDTO.PROBLEM_CTRL_REMOVE.equals(item.getCtrl()))
                .map(Issue6sReviewRectifyDTO.Problem::getId).toList();
        // 需要添加的问题
        List<Issue6sReviewRectifyDTO.Problem> addProblems = problems.stream().filter(item -> Issue6sReviewRectifyDTO.PROBLEM_CTRL_ADD.equals(item.getCtrl())).toList();
        // 默认提交的问题
        List<Issue6sReviewRectifyDTO.Problem> normalProblems = problems.stream().filter(item -> Issue6sReviewRectifyDTO.PROBLEM_CTRL_NORMAL.equals(item.getCtrl())).toList();


        List<Cmt6sReviewProblem> problemList = new ArrayList<>();
        // 需要新关联的图片
        List<AttachmentOwnerSaveBO> updateProblemImages = new ArrayList<>();
        // 需要移除的图片
        List<String> removedProblemImageIds = new ArrayList<>();
        // 追加需要更新的问题
        this.addNeedUpdateProblems(updateProblems, problemList, updateProblemImages, removedProblemImageIds);

        // 追加默认提交的问题更新协助人
        problemList.addAll(normalProblems.stream().map(item -> {
            Cmt6sReviewProblem problem = new Cmt6sReviewProblem();
            problem.setId(item.getId());
            problem.setAssister(item.getAssister());
            return problem;
        }).toList());
        // 添加新的问题
        this.addNewProblem(dto.getId(), addProblems, updateProblemImages);
        // 更新问题
        if (!problemList.isEmpty()) {
            cmt6sReviewProblemService.updateBatchById(problemList);
        }
        // 删除问题
        if (!removeProblemIds.isEmpty()) {
            cmt6sReviewProblemService.removeByIds(removeProblemIds);
        }
        // 移除问题图片
        attachmentService._removeByIds(removedProblemImageIds);
        // 关联新的问题图片
        attachmentService.saveAttachmentsOwner(updateProblemImages);
        // 更新6S评审
        this.lambdaUpdate()
                .set(Cmt6sReview::getResponsiblePersonId, dto.getResponsiblePersonId())
                .set(Cmt6sReview::getStatus, CmtLocalConstants._6S_REVIEW_STATUS.PENDING_RECTIFY)
                .eq(Cmt6sReview::getId, dto.getId())
                .update();
    }

    /**
     * 添加需要更新或移除的问题数据
     *
     */
    private void addNeedUpdateProblems(List<Issue6sReviewRectifyDTO.Problem> updateProblems, List<Cmt6sReviewProblem> problemList,
                                       List<AttachmentOwnerSaveBO> updateProblemImages, List<String> removedProblemImageIds) {
        // 存在需要更新的问题
        if (!updateProblems.isEmpty()) {
            updateProblems.forEach(item -> {
                // 有新添加的问题图片
                if (!item.getNewImageIds().isEmpty()) {
                    item.getNewImageIds().forEach(imageId -> {
                        AttachmentOwnerSaveBO bo = new AttachmentOwnerSaveBO(imageId, AttachmentOwnerType.CMT_6S_REVIEW_PROBLEM.getCode(), item.getId());
                        updateProblemImages.add(bo);
                    });
                }
                // 有移除的问题图片
                if (!item.getRemovedImageIds().isEmpty()) {
                    removedProblemImageIds.addAll(item.getRemovedImageIds());
                }
                Cmt6sReviewProblem problem = new Cmt6sReviewProblem();
                // 问题或者建议更新了
                if (GlobalConstants.INT_YES.equals(item.getFieldIsUpdate())) {
                    problem.setId(item.getId());
                    problem.setDescription(item.getTitle());
                    problem.setSuggestion(item.getSuggestion());
                }
                problem.setAssister(item.getAssister());
                problemList.add(problem);
            });
        }
    }


    /**
     * 增加新的问题
     */
    private void addNewProblem(String reviewId, List<Issue6sReviewRectifyDTO.Problem> addProblems, List<AttachmentOwnerSaveBO> updateProblemImages) {
        addProblems.forEach(item -> {
            Cmt6sReviewProblem problem = new Cmt6sReviewProblem();
            problem.setReviewId(reviewId);
            problem.setSuggestion(item.getSuggestion());
            problem.setAssister(item.getAssister());
            problem.setDescription(item.getTitle());
            cmt6sReviewProblemService.save(problem);
            List<AttachmentOwnerSaveBO> problemImages = item.getImages().stream()
                    .map(image -> new AttachmentOwnerSaveBO(image.getId(),
                            AttachmentOwnerType.CMT_6S_REVIEW_PROBLEM.getCode(), problem.getId()))
                    .toList();
            updateProblemImages.addAll(problemImages);
        });
    }
}
