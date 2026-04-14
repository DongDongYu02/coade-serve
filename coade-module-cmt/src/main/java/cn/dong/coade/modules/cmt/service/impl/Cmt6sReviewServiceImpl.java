package cn.dong.coade.modules.cmt.service.impl;

import cn.dong.coade.modules.cmt.constants.CmtLocalConstants;
import cn.dong.coade.modules.cmt.domain.dto.Cmt6sReviewDTO;
import cn.dong.coade.modules.cmt.domain.dto.Issue6sReviewRectifyDTO;
import cn.dong.coade.modules.cmt.domain.entity.Cmt6sReview;
import cn.dong.coade.modules.cmt.domain.entity.Cmt6sReviewProblem;
import cn.dong.coade.modules.cmt.domain.entity.CmtDepartment;
import cn.dong.coade.modules.cmt.domain.entity.CmtUser;
import cn.dong.coade.modules.cmt.domain.query.Cmt6sReviewQuery;
import cn.dong.coade.modules.cmt.domain.vo.Cmt6sReviewDetailVO;
import cn.dong.coade.modules.cmt.domain.vo.Cmt6sReviewStatusCountVO;
import cn.dong.coade.modules.cmt.domain.vo.Cmt6sReviewVO;
import cn.dong.coade.modules.cmt.mapper.Cmt6sReviewMapper;
import cn.dong.coade.modules.cmt.service.AI6sService;
import cn.dong.coade.modules.cmt.service.ICmt6sReviewProblemService;
import cn.dong.coade.modules.cmt.service.ICmt6sReviewService;
import cn.dong.nexus.common.api.ICommonAttachmentService;
import cn.dong.nexus.common.constants.ApiConstants;
import cn.dong.nexus.common.constants.AttachmentOwnerType;
import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.common.domain.bo.AttachmentBO;
import cn.dong.nexus.common.domain.bo.AttachmentOwnerSaveBO;
import cn.dong.nexus.common.domain.vo.AttachmentVO;
import cn.dong.nexus.core.api.ApiMessage;
import cn.dong.nexus.core.config.properties.CoadeProperties;
import cn.dong.nexus.core.exception.BizException;
import cn.dong.nexus.core.resmapping.ResMappingUtil;
import cn.dong.nexus.core.util.PageUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class Cmt6sReviewServiceImpl extends ServiceImpl<Cmt6sReviewMapper, Cmt6sReview> implements ICmt6sReviewService {

    private final ICommonAttachmentService attachmentService;
    private final ICmt6sReviewProblemService cmt6sReviewProblemService;
    private final AI6sService ai6sService;
    private final CoadeProperties coadeProperties;
    private final RestTemplate restTemplate;

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
    public void reAnalyze(String reviewId) {
        Cmt6sReview review = this.getById(reviewId);
        if (Objects.isNull(review)) {
            throw new BizException(ApiMessage.NOT_FOUND);
        }
        List<String> attachmentIds = attachmentService.getByOwners(AttachmentOwnerType.CMT_6S_REVIEW,
                List.of(reviewId)).stream().map(AttachmentBO::getId).toList();
        this.lambdaUpdate().set(Cmt6sReview::getStatus, CmtLocalConstants._6S_REVIEW_STATUS.IN_ANALYSIS)
                .eq(Cmt6sReview::getId, reviewId)
                .update();
        ai6sService.analyze(reviewId, attachmentIds);
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
        detail.setMaterials(materials.stream().map(item -> new AttachmentVO(item.getId(), coadeProperties.getFileAccessUrl() + item.getPath())).toList());

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
            item.setImages(images.stream().map(img -> new AttachmentVO(img.getId(), coadeProperties.getFileAccessUrl() + img.getPath())).collect(Collectors.toList()));
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
        // 发起 EKP流程
        String ekpReviewId = this.initiateEkp6sRectifyReview(dto, review);
        // 更新6S评审
        this.lambdaUpdate()
                .set(Cmt6sReview::getEkpReviewId, ekpReviewId)
                .set(Cmt6sReview::getResponsiblePersonId, dto.getResponsiblePersonId())
                .set(Cmt6sReview::getStatus, CmtLocalConstants._6S_REVIEW_STATUS.PENDING_RECTIFY)
                .eq(Cmt6sReview::getId, dto.getId())
                .update();

    }

    @Override
    public void rectifyCompleted(String ekpReviewId) {
        Cmt6sReview review = this.lambdaQuery().eq(Cmt6sReview::getEkpReviewId, ekpReviewId).one();
        if (Objects.isNull(review)) {
            log.error("EKP回调整改完成失败，未找到关联的6S评审记录，ekpReviewId={}", ekpReviewId);
        }
        this.lambdaUpdate()
                .set(Cmt6sReview::getStatus, CmtLocalConstants._6S_REVIEW_STATUS.COMPLETED)
                .eq(Cmt6sReview::getEkpReviewId, ekpReviewId)
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

    private String initiateEkp6sRectifyReview(Issue6sReviewRectifyDTO dto, Cmt6sReview review) {
        // 6S整改标题
        String docSubject = StrUtil.format(review.getTitle());
        // 创建人
        String ekpId = ResMappingUtil.getFieldMappingValue(review.getCreateBy(), CmtUser::getId, CmtUser::getEkpId);
        String docCreator = new JSONObject().set("Id", ekpId).toJSONString(1);
        JSONObject content = new JSONObject();
        // 责任部门
        String depId = ResMappingUtil.getFieldMappingValue(review.getDeptId(), CmtDepartment::getId, CmtDepartment::getEkpOrgId);
        content.set("fd_3e8b05b852e42c", new JSONObject().set("Id", depId));
        // 责任人
        content.set("fd_3e8b05c3b915ce", new JSONObject().set("Id", dto.getResponsiblePersonId()));

        MultiValueMap<String, Object> wholeForm = new LinkedMultiValueMap<>();

        // 整改项
        JSONArray items = new JSONArray();
        for (int i = 0; i < dto.getProblems().size(); i++) {
            Issue6sReviewRectifyDTO.Problem problem = dto.getProblems().get(i);
            String attKey = UUID.fastUUID().toString(true);
            JSONObject item = new JSONObject()
                    // 整改内容
                    .set("fd_3e8b057dd5931c.fd_3e8b06cf4a9d4c", problem.getTitle())
                    // 截止日期
                    .set("fd_3e8b057dd5931c.fd_3e8b06d20587fe", LocalDateTimeUtil.format(problem.getDeadline(), GlobalConstants.DatePattern.Y_M_D_H_M))
                    // 协助人
                    .set("fd_3e8b057dd5931c.fd_3e8b08373a1ea4", new JSONObject().set("Id", dto.getResponsiblePersonId()))
                    // 问题照片
                    .set("fd_3e8b057dd5931c.fd_3e8b05f375483e", attKey);
            String attForm = StrUtil.format("attachmentForms[{}]", i);
            wholeForm.add(attForm + ".fdKey", attKey);
            wholeForm.add(attForm + ".fdFileName", StrUtil.format("{}.png", RandomUtil.randomString(5)));
            String imagePath = coadeProperties.getFileUploadPath() + problem.getImages().getFirst().getPath().replace(coadeProperties.getFileAccessUrl(), "");
            wholeForm.add(attForm + ".fdAttachment", new FileSystemResource(new File(imagePath)));
            items.add(item);
        }
        content.set("fd_3e8b057dd5931c", items);
        wholeForm.add("docSubject", docSubject);
        wholeForm.add("docCreator", docCreator);
        wholeForm.add("docStatus", 20);
        wholeForm.add("fdTemplateId", "199e1d2c5cff3ef9e9b53a346f0ab173");
        wholeForm.add("formValues", content.toJSONString(1));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(wholeForm, headers);

        String url = coadeProperties.getEkp().getServerUrl() + ApiConstants.INITIATE_EKP_REVIEW;

        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        if (Objects.isNull(resp.getBody()) || StrUtil.isBlank(resp.getBody())) {
            log.error("发起补卡申请到EKP审批失败，EKP接口返回异常，url={}, body={}", url, resp.getBody());
            throw new BizException(ApiMessage.INTERNAL_ERROR);
        }
        if (JSONUtil.isTypeJSON(resp.getBody())) {
            log.error("发起补卡申请到EKP审批失败，EKP接口返回异常，url={}, body={}", url, resp.getBody());
        }
        return resp.getBody();

    }
}
