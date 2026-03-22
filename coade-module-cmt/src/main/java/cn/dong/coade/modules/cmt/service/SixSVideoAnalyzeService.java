package cn.dong.coade.modules.cmt.service;

import cn.dong.coade.modules.cmt.domain.bo.Cmt6SProblemResultBO;
import cn.dong.coade.modules.cmt.domain.bo.SixSVideoResult;
import cn.dong.nexus.common.utils.FfmpegUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class SixSVideoAnalyzeService {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    public void test() throws IOException {
        String prompt = """
                你是资深制造企业6S审核专家，请根据视频内容进行现场评审。
                
                评分维度：
                1. 整理
                2. 整顿
                3. 清扫
                4. 清洁
                5. 素养
                6. 安全
                
                请输出：
                - 现场概述
                - 每个维度的主要问题
                - 每个问题的整改建议
                - 风险等级
                - 总分（0-100）
                
                输出要求：
                1. 只输出 JSON，不要输出 markdown，不要输出解释文字
                2. 顶层结构为：
                [
                    {
                      "dimension": "sort/setInOrder/shine/standardize/sustain/safety",
                      "title": "问题标题",
                      "evidence": "证据描述",
                      "time": "mm:ss",
                      "timeRange": "mm:ss-mm:ss",
                      "severity": "low/medium/high",
                      "deduction": 0,
                      "suggestion": "整改建议",
                      "imageUrl": ""
                    }
                ]
                3. 如果没有识别到问题，返回：
                {
                  "problems": []
                }
                4. 仅根据视频可见内容判断
                5. time 和 timeRange 尽量精确到秒
                6. 使用中文回答
                """;
        UserMessage userMessage = UserMessage.from(
                TextContent.from(prompt),
                VideoContent.from("http://oa.zjkede.com:5100/file/20260206/1.mp4")
        );

        ChatResponse response = chatModel.chat(userMessage);
        String json = response.aiMessage().text();
        JSONArray objects = JSONUtil.parseArray(json);
        List<Cmt6SProblemResultBO> list = JSONUtil.toList(objects, Cmt6SProblemResultBO.class);
        this.fillProblemImageUrls("\"C:\\\\Users\\\\Administrator\\\\Desktop\\\\image\\\\1.mp4\"", list);
        System.out.println();
    }

    private static final String FRAME_OUTPUT_DIR = "D:/frames/";
    private static final String FRAME_ACCESS_PREFIX = "/sixs/frames/";

    public List<Cmt6SProblemResultBO> fillProblemImageUrls(String videoPath, List<Cmt6SProblemResultBO> problems) {
        if (CollUtil.isEmpty(problems)) {
            return problems;
        }

        File dir = new File(FRAME_OUTPUT_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        for (int i = 0; i < problems.size(); i++) {
            Cmt6SProblemResultBO item = problems.get(i);
            Integer second = resolveSecond(item);
            if (second == null) {
                continue;
            }

            String fileName = buildFrameFileName(i, item);
            String outputImagePath = FRAME_OUTPUT_DIR + fileName;

            FfmpegUtil.extractFrame(videoPath, second, outputImagePath);

        }

        return problems;
    }

    private Integer resolveSecond(Cmt6SProblemResultBO item) {
        if (item == null) {
            return null;
        }
        if (StrUtil.isNotBlank(item.getTime())) {
            return parseTimeToSecond(item.getTime());
        }
        if (StrUtil.isNotBlank(item.getTimeRange())) {
            String startTime = item.getTimeRange().split("-")[0].trim();
            return parseTimeToSecond(startTime);
        }
        return null;
    }

    private Integer parseTimeToSecond(String time) {
        String[] arr = time.trim().split(":");
        try {
            if (arr.length == 2) {
                return Integer.parseInt(arr[0]) * 60 + Integer.parseInt(arr[1]);
            }
            if (arr.length == 3) {
                return Integer.parseInt(arr[0]) * 3600
                       + Integer.parseInt(arr[1]) * 60
                       + Integer.parseInt(arr[2]);
            }
        } catch (Exception e) {
            throw new RuntimeException("时间解析失败: " + time, e);
        }
        throw new RuntimeException("不支持的时间格式: " + time);
    }

    private String buildFrameFileName(int index, Cmt6SProblemResultBO item) {
        String title = StrUtil.blankToDefault(item.getTitle(), "problem");
        title = title.replaceAll("[\\\\/:*?\"<>|\\s]+", "_");
        return System.currentTimeMillis() + "_" + index + "_" + title + "_"
               + UUID.randomUUID().toString().replace("-", "") + ".jpg";
    }

    public SixSVideoResult analyzeAllFramesOnce(String frameDir) {
        try {
            List<Path> imagePaths = listImages(frameDir);
            if (imagePaths.isEmpty()) {
                throw new IllegalArgumentException("抽帧目录下没有图片: " + frameDir);
            }

            List<Content> contents = new ArrayList<>();

            String prompt = """
                    你是企业6S巡检评分助手。
                    下面会一次性给你多张同一段巡检视频抽出的连续帧图片。
                    请把这些图片当作同一次巡检的整体材料，做“整体判断”，不要逐张分别输出。
                    
                    请完成：
                    1. 综合判断现场整体整洁度、规范度、安全风险
                    2. 识别重复出现的问题，不要重复计数
                    3. 判断问题是偶发还是在多个画面中持续存在
                    4. 给出一个总分，而不是每张图一个分数
                    5. 输出最终整改建议
                    
                    评分重点：
                    - 地面垃圾、污渍、积水
                    - 物品乱堆乱放
                    - 通道占用
                    - 消防器材遮挡
                    - 工位/桌面/设备表面凌乱
                    - 明显安全隐患
                    
                    只返回 JSON，不要 markdown，不要解释：
                    
                    {
                      "score": 0,
                      "level": "优秀/良好/一般/较差",
                      "summary": "整体结论",
                      "issues": [
                        {
                          "type": "问题类型",
                          "severity": "low|medium|high",
                          "description": "问题描述",
                          "evidenceFrames": ["frame_001.jpg", "frame_004.jpg"]
                        }
                      ],
                      "advice": [
                        "整改建议1",
                        "整改建议2"
                      ]
                    }
                    """;

            contents.add(TextContent.from(prompt));

            for (Path imagePath : imagePaths) {
                byte[] bytes = Files.readAllBytes(imagePath);
                String base64 = Base64.getEncoder().encodeToString(bytes);

                String mimeType = Files.probeContentType(imagePath);
                if (mimeType == null || mimeType.isBlank()) {
                    String fileName = imagePath.getFileName().toString().toLowerCase();
                    mimeType = fileName.endsWith(".png") ? "image/png" : "image/jpeg";
                }

                contents.add(ImageContent.from(base64, mimeType));
            }

            UserMessage userMessage = UserMessage.from(contents);

            ChatResponse response = chatModel.chat(userMessage);
            String json = response.aiMessage().text();

            return objectMapper.readValue(json, SixSVideoResult.class);

        } catch (IOException e) {
            throw new RuntimeException("一次性分析所有抽帧图片失败", e);
        }
    }

    private List<Path> listImages(String frameDir) throws IOException {
        try (Stream<Path> stream = Files.list(Paths.get(frameDir))) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        String name = path.getFileName().toString().toLowerCase();
                        return name.endsWith(".jpg")
                               || name.endsWith(".jpeg")
                               || name.endsWith(".png");
                    })
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .toList();
        }
    }
}