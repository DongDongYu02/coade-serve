package cn.dong.coade.modules.cmt.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("cmt_6s_review_problem")
public class Cmt6sReviewProblem {

    private String id;

    private String reviewId;

    private String description;

    private String suggestion;

    private String assister;


}
