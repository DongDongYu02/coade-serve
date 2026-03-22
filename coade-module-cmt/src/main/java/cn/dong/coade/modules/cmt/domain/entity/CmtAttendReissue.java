package cn.dong.coade.modules.cmt.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("cmt_attend_reissue")
public class CmtAttendReissue {

    private String id;

    private String cmtUserId;

    private String ekpUserId;

    private String ekpReviewId;

    private LocalDateTime checkinTime;

    private LocalDateTime ruleCheckinTime;

    private Integer isApproved;

    private String reissueType;

    private String reason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;


}
