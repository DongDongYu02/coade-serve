package cn.dong.coade.modules.cmt.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("cmt_leave_request")
public class CmtLeaveRequest {

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
     * 请假类别 2事假 3病假
     */
    private Integer type;

    /**
     * 请假开始时间
     */
    private LocalDateTime beginTime;

    /**
     * 请假结束时间
     */
    private LocalDateTime endTime;

    /**
     * 请假时长
     */
    private BigDecimal duration;

    /**
     * 请假原因
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
