package cn.dong.coade.modules.cmt.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("cmt_cleaning_record")
public class CmtCleaningRecord {

    /**
     * ID
     */
    private String id;

    /**
     * 区域ID
     */
    private String areaId;

    /**
     * 保洁人员ID
     */
    private String principalUserId;

    /**
     * 保洁人员名字
     */
    private String principalUserName;

    private String remark;

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

    @TableLogic
    private Integer delFlag;

}
