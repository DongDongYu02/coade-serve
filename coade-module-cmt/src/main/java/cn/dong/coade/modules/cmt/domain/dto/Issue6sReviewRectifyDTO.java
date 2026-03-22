package cn.dong.coade.modules.cmt.domain.dto;

import cn.dong.nexus.common.domain.dto.AttachmentDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "发起6S评审整改 DTO")
public class Issue6sReviewRectifyDTO {

    @Schema(description = "ID")
    @NotBlank
    private String id;

    @Schema(description = "责任人 ID")
    @NotBlank
    private String responsiblePersonId;

    @Schema(description = "整改问题")
    @NotEmpty
    @Valid
    private List<Problem> problems;

    @Data
    public static class Problem {

        @Schema(description = "问题 ID")
        private String id;

        @Schema(description = "发起状态 1默认 2修改 3移除 4新增")
        @NotNull
        private Integer ctrl;

        @Schema(description = "问题点")
        @NotBlank
        private String title;

        @Schema(description = "整改建议")
        @NotBlank
        private String suggestion;

        @Schema(description = "协助人")
        private String assister;

        @Schema(description = "问题图片")
        @NotEmpty
        private List<AttachmentDTO> images;

        @Schema(description = "新增的问题图片")
        private List<String> newImageIds;

        @Schema(description = "移除的问题图片")
        private List<String> removedImageIds;

        @Schema(description = "是否有字段内容更新 0否 1是")
        private Integer fieldIsUpdate = 0;

    }

    public static final Integer PROBLEM_CTRL_NORMAL = 1;
    public static final Integer PROBLEM_CTRL_UPDATE = 2;
    public static final Integer PROBLEM_CTRL_REMOVE = 3;
    public static final Integer PROBLEM_CTRL_ADD = 4;
}
