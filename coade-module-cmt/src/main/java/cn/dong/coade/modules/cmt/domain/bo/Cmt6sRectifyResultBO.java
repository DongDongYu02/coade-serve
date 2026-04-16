package cn.dong.coade.modules.cmt.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NotNull
public class Cmt6sRectifyResultBO {
    private String problemId;

    private String description;

    private String attKey;

    private String attPath;

    private String originName;

    public Cmt6sRectifyResultBO(String description, String attKey, String problemId) {
        this.description = description;
        this.attKey = attKey;
        this.problemId = problemId;
    }
}
