package cn.dong.coade.modules.cmt.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WecomLoginDTO {

    @NotBlank
    private String code;

    private String state;
}
