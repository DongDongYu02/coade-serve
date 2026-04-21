package cn.dong.coade.modules.cmt.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("cmt_biz_trip_request")
public class CmtBizTripRequest {

    private String id;

    private String userId;

    private String userEkpId;

    private LocalDateTime beginTime;

    private LocalDateTime endTime;

    private BigDecimal duration;

    private String reason;

    private String ekpReviewId;

    private Integer status;

    private String createBy;

    private LocalDateTime createTime;

}
