package cn.dong.coade.modules.cmt.domain.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Cmt6sReviewProblemUpdateBO {

    private String id;

    private String description;

    private String suggestion;

    private String assister;

}
