package cn.dong.nexus.modules.system.domain.entity;

import cn.dong.nexus.core.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_data_dict")
public class SysDataDict extends BaseEntity {

    private String name;

    private String code;

    private Integer sort;

}
