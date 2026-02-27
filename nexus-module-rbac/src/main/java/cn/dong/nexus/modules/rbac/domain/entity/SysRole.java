package cn.dong.nexus.modules.rbac.domain.entity;

import cn.dong.nexus.core.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_role")
public class SysRole extends BaseEntity {

    private String name;

    private String code;

    private String description;

    private Integer status;


}
