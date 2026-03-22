package cn.dong.coade.modules.cmt.service.impl;

import cn.dong.coade.modules.cmt.constants.CmtLocalConstants;
import cn.dong.coade.modules.cmt.domain.bo.Cmt6SProblemResultBO;
import cn.dong.coade.modules.cmt.domain.entity.Cmt6sReview;
import cn.dong.coade.modules.cmt.domain.entity.Cmt6sReviewProblem;
import cn.dong.coade.modules.cmt.mapper.Cmt6sReviewProblemMapper;
import cn.dong.coade.modules.cmt.service.ICmt6sReviewProblemService;
import cn.dong.nexus.common.api.CommonAttachmentService;
import cn.dong.nexus.common.constants.AttachmentOwnerType;
import cn.dong.nexus.common.constants.FileMimeType;
import cn.dong.nexus.common.domain.bo.AttachmentBO;
import cn.dong.nexus.common.utils.FfmpegUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class Cmt6sReviewProblemServiceImpl extends ServiceImpl<Cmt6sReviewProblemMapper, Cmt6sReviewProblem> implements ICmt6sReviewProblemService {
    @Value("${nexus.file-upload-path}")
    private String uploadPath;
    private final CommonAttachmentService attachmentService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void extractProblemFrameAndSave(String recordId, List<Cmt6SProblemResultBO> problem) {
        if (CollUtil.isEmpty(problem)) return;
        List<Cmt6SProblemResultBO> imageProblems = problem.stream().filter(item -> "image".equals(item.getFileType())).toList();
        List<Cmt6SProblemResultBO> videos = problem.stream().filter(item -> "video".equals(item.getFileType())).toList();
        if (CollUtil.isNotEmpty(imageProblems)) {
            for (Cmt6SProblemResultBO item : imageProblems) {
                // 保存问题
                Cmt6sReviewProblem record = new Cmt6sReviewProblem();
                record.setDescription(item.getEvidence());
                record.setReviewId(recordId);
                record.setSuggestion(item.getSuggestion());
                record.setReviewId(recordId);
                this.save(record);
                AttachmentBO attachmentBO = attachmentService.getBoById(item.getSourceId());
                attachmentBO.setId(null);
                attachmentBO.setCreateBy(null);
                attachmentBO.setCreateTime(null);
                attachmentBO.setOwnerId(record.getId());
                attachmentBO.setOwnerType(AttachmentOwnerType.CMT_6S_REVIEW_PROBLEM.getCode());
                attachmentService.save(attachmentBO);
            }
        }
        if (CollUtil.isNotEmpty(videos)) {
            // 抽帧获取图片
            String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            File frameOutputDir = new File(uploadPath + today);
            FileUtil.mkdir(frameOutputDir);
            for (Cmt6SProblemResultBO item : videos) {
                // 保存问题
                Cmt6sReviewProblem record = new Cmt6sReviewProblem();
                record.setDescription(item.getEvidence());
                record.setReviewId(recordId);
                record.setSuggestion(item.getSuggestion());
                record.setReviewId(recordId);
                this.save(record);
                // 视频路径
                String videoPath = uploadPath + item.getFilePath();
                // 问题秒数
                List<Integer> secondsList = parseSeconds(item.getTimeRange());
                // 抽帧保存问题图片
                List<String> paths = secondsList.stream().map(seconds -> {
                    String fileName = buildFrameFileName(seconds, item);
                    String outputImagePath = frameOutputDir + "/" + fileName;
                    FfmpegUtil.extractFrame(videoPath, seconds, outputImagePath);
                    return today + "/" + fileName;
                }).toList();
                List<String> attachmentIds = attachmentService.saveByPaths(paths);
                attachmentService.saveAttachmentsOwner(attachmentIds, AttachmentOwnerType.CMT_6S_REVIEW_PROBLEM, record.getId());
            }
        }

    }

    public static List<Integer> parseSeconds(String timeRange) {
        String[] parts = timeRange.split("-");
        if (parts.length != 2) {
            throw new IllegalArgumentException("时间范围格式错误: " + timeRange);
        }

        int start = parseToSecond(parts[0]);
        int end = parseToSecond(parts[1]);

        if (start > end) {
            throw new IllegalArgumentException("开始时间不能大于结束时间: " + timeRange);
        }

        List<Integer> result = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            result.add(i);
        }
        return result;
    }

    private static int parseToSecond(String time) {
        String[] arr = time.split(":");
        if (arr.length != 2) {
            throw new IllegalArgumentException("时间格式必须为 mm:ss : " + time);
        }

        int minute = Integer.parseInt(arr[0]);
        int second = Integer.parseInt(arr[1]);
        return minute * 60 + second;
    }


    private String buildFrameFileName(int index, Cmt6SProblemResultBO item) {
        String title = StrUtil.blankToDefault(item.getTitle(), "problem");
        title = title.replaceAll("[\\\\/:*?\"<>|\\s]+", "_");
        return System.currentTimeMillis() + "_" + index + "_" + title + "_"
               + UUID.randomUUID().toString().replace("-", "") + ".jpg";
    }

}
