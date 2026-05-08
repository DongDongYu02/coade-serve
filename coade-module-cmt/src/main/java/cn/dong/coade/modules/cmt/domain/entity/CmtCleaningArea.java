package cn.dong.coade.modules.cmt.domain.entity;

import cn.dong.nexus.core.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cmt_cleaning_area")
public class CmtCleaningArea extends BaseEntity {

    /**
     * ID
     */
    private String id;

    /**
     * 保洁区域
     */
    private String name;

    /**
     * 负责人ID
     */
    private String principalUserId;

    private String rule;

    private String remark;

}
