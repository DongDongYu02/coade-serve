package cn.dong.coade.modules.cmt.service;

import cn.dong.coade.modules.cmt.constants.CmtLocalConstants;
import cn.dong.coade.modules.cmt.domain.bo.Cmt6SProblemResultBO;
import cn.dong.coade.modules.cmt.domain.entity.Cmt6sReview;
import cn.dong.nexus.common.api.CommonAttachmentService;
import cn.dong.nexus.common.constants.FileMimeType;
import cn.dong.nexus.common.domain.bo.AttachmentBO;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import dev.langchain4j.data.message.*;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AI6sService {

    private final ChatModel chatModel;
    private final CommonAttachmentService attachmentService;
    private final ICmt6sReviewProblemService cmt6sReviewProblemService;

    @Value("${nexus.file-access-url}")
    private String fileAccessUrl;


    @Async
    public void analyze(String recordId, List<String> attachmentIds) {
        // 查询附件
        List<AttachmentBO> attachments = attachmentService.getByIds(attachmentIds);
        if (attachments.isEmpty()) {
            this.set6sReviewStatusToFailed(recordId);
            log.error("6S评审AI分析失败：未查询到相关附件，记录ID：{}", recordId);
            return;
        }
        String prompt = ResourceUtil.readStr("prompts/6s-prompt.txt", StandardCharsets.UTF_8);
        List<Content> contents = new ArrayList<>();
        contents.add(TextContent.from(prompt));

        attachments.forEach(item -> {
            String url = fileAccessUrl + item.getPath();
            FileMimeType type = this.detectFileByMime(item.getMime());
            contents.add(TextContent.from(StrUtil.format("""
                        文件开始
                        sourceId: {}
                        filePath: {}
                        请基于紧随其后的这个文件进行观察
                    """, item.getId(), item.getPath())));
            switch (type) {
                case IMAGE -> contents.add(ImageContent.from(url));
                case VIDEO -> contents.add(VideoContent.from(url));
            }
        });
        UserMessage userMessage = UserMessage.from(contents);
        log.info("调用大模型开始分析，等待结果...");
        try {
            ChatResponse response = chatModel.chat(userMessage);
            String json = this.normalizeAiResponse(response.aiMessage().text());
            if (!JSONUtil.isTypeJSON(json)) {
                this.set6sReviewStatusToFailed(recordId);
                log.error("6S评审AI分析失败：AI返回的结果解析失败，记录ID：{}，AI Response:{}", recordId, json);
                return;
            }
            JSONObject result = JSONUtil.parseObj(json);
            if (!result.containsKey("existProblem")) {
                this.set6sReviewStatusToFailed(recordId);
                log.error("6S评审AI分析失败：AI返回的结果解析失败，记录ID：{}，AI Response:{}", recordId, json);
                return;
            }
            boolean existProblem = result.getBool("existProblem");
            if (existProblem) {
                // 存在问题
                JSONArray problem = result.getJSONArray("problem");
                List<Cmt6SProblemResultBO> problemResult = JSONUtil.toList(problem, Cmt6SProblemResultBO.class);
                cmt6sReviewProblemService.extractProblemFrameAndSave(recordId, problemResult);
            }
            Db.lambdaUpdate(Cmt6sReview.class)
                    .set(Cmt6sReview::getStatus, CmtLocalConstants._6S_REVIEW_STATUS.ANALYSIS_COMPLETED)
                    .eq(Cmt6sReview::getId, recordId)
                    .update();
        } catch (Exception e) {
            this.set6sReviewStatusToFailed(recordId);
            log.error("6S评审AI分析失败，记录ID：{}，message: {}", recordId, e.getMessage());
        }

    }

    /**
     * 将6S评审状态设置为分析失败
     *
     * @param recordId 评审 ID
     */
    private void set6sReviewStatusToFailed(String recordId) {
        Db.lambdaUpdate(Cmt6sReview.class)
                .set(Cmt6sReview::getStatus, CmtLocalConstants._6S_REVIEW_STATUS.ANALYSIS_FAILED)
                .eq(Cmt6sReview::getId, recordId)
                .update();
    }


    private String normalizeAiResponse(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');

        if (start == -1 || end == -1 || start > end) {
            return null;
        }

        return text.substring(start, end + 1);
    }

    private FileMimeType detectFileByMime(String mime) {
        if (mime == null || mime.isBlank()) {
            return FileMimeType.UNKNOWN;
        }
        mime = mime.toLowerCase().trim();

        if (mime.startsWith("image/")) {
            return FileMimeType.IMAGE;
        }
        if (mime.startsWith("video/")) {
            return FileMimeType.VIDEO;
        }
        return FileMimeType.UNKNOWN;
    }
}
