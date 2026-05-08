package cn.dong.coade.modules.cmt.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("cmt_issue_demand")
public class CmtIssueDemand {

    private String id;

    private String serialNo;

    /**
     * 标题
     */
    private String title;

    /**
     * 类型 1问题反馈 2需求开发
     */
    private Integer type;

    /**
     * 所属系统 数据字典
     */
    private Integer systemType;

    private String otherSystem;

    /**
     * 问题/需求描述
     */
    private String description;

    /**
     * 提出部门
     */
    private String proposeDept;

    /**
     * 提出人 cmt_user_id
     */
    private String proposeUserId;

    private String proposeUserName;

    /**
     * 负责人
     */
    private String principalUserId;

    private String principalUserName;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 驳回原因
     */
    private String rejectReason;

    /**
     * 作废原因
     */
    private String voidedReason;

    /**
     * 期望完成时间
     */
    private LocalDateTime expectedFinishTime;

    /**
     * 计划完成时间
     */
    private LocalDateTime planFinishTime;

    /**
     * 开发开始时间
     */
    private LocalDateTime devStartTime;


    /**
     * 实际完成时间
     */
    private LocalDateTime actualFinishTime;

    /**
     * 作废时间
     */
    private LocalDateTime voidedTime;

    /**
     * 验收通过时间
     */
    private LocalDateTime acceptanceTime;

    /**
     * 结果反馈
     */
    private String resultFeedback;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 创建端
     */
    @TableField(fill = FieldFill.INSERT)
    private String createClient;

    /**
     * 逻辑删除
     */
    private Integer delFlag;

    /**
     * 创建人名称
     */
    @TableField(fill = FieldFill.INSERT)
    private String creator;

    /**
     * 是否紧急
     */
    private Integer isUrgent;


}
