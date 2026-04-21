package cn.dong.coade.modules.cmt.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("cmt_outgoing_request")
public class CmtOutgoingRequest {

    private String id;

    /**
     * CMT用户ID
     */
    private String userId;

    /**
     * EKP用户ID
     */
    private String userEkpId;

    /**
     * 外出日期
     */
    private LocalDate outDate;

    /**
     * 外出时间开始
     */
    private LocalTime outTimeBegin;

    /**
     * 外出时间结束
     */
    private LocalTime outTimeEnd;

    /**
     * 外出时长
     */
    private BigDecimal duration;

    /**
     * 外出事由
     */
    private String reason;

    /**
     * EKP流程ID
     */
    private String ekpReviewId;

    /**
     * 0审批中 1审批通过 2审批驳回 3撤销
     */
    private Integer status;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
