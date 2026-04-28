package cn.dong.coade.modules.cmt.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("cmt_biz_trip_request")
public class CmtBizTripRequest {

    private String id;

    private String userId;

    private String userEkpId;

    private LocalDate beginTime;

    private LocalDate endTime;

    private BigDecimal duration;

    private String durationFormat;

    private String reason;

    private String ekpReviewId;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

}
