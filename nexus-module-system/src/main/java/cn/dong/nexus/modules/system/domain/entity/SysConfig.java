package cn.dong.nexus.modules.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_config")
public class SysConfig {

    @TableId("`key`")
    @TableField("`key`")
    private String key;

    @TableField("`value`")
    private String value;

    private String description;

    @TableField("`group`")
    private Integer group;

    private LocalDateTime updateTime;

    private String updateBy;

}
