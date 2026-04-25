package cn.dong.coade.modules.cmt.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("cmt_overtime_request")
public class CmtOvertimeRequest {

    /**
     * ID
     */
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
     * 加班日期
     */
    private LocalDate overtimeDate;

    /**
     * 开始时间
     */
    private LocalTime beginTime;

    /**
     * 结束时间
     */
    private LocalTime endTime;

    /**
     * 加班时长
     */
    private BigDecimal duration;

    /**
     * 加班事由
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
