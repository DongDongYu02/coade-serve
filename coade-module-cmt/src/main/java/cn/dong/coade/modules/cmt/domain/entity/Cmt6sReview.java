package cn.dong.coade.modules.cmt.domain.entity;

import cn.dong.nexus.core.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cmt_6s_review")
public class Cmt6sReview extends BaseEntity {

    private String id;

    private String title;

    private String deptId;

    private Integer status;

    private String responsiblePersonId;

}
