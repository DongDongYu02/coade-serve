package cn.dong.coade.modules.cmt.domain.dto;

import cn.dong.coade.modules.cmt.domain.entity.Cmt6sReview;
import cn.dong.nexus.core.base.BaseDTO;
import cn.dong.nexus.core.valid.BizValidate;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "6S评审 DTO")
public class Cmt6sReviewDTO extends BaseDTO<Cmt6sReview> {

    @Schema(description = "评审标题")
    @NotBlank
    @BizValidate.Unique(message = "评审标题重复，请重新填写！")
    private String title;

    @Schema(description = "部门 ID")
    @NotBlank
    private String deptId;

    @Schema(description = "附件 ID集合")
    @NotEmpty
    private List<String> attachmentIds;
}
