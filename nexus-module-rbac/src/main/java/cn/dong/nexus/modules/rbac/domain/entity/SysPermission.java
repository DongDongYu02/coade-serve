package cn.dong.nexus.modules.rbac.domain.entity;

import cn.dong.nexus.core.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("sys_permission")
public class SysPermission extends BaseEntity {

    private String name;

    private String pid;

    private String path;

    private String redirect;

    private String component;

    private String authCode;

    private Integer type;

    private String icon;

    private Integer status;

    private Integer affix;

    private Integer keepAlive;

    private Integer hidden;

    private Integer sort;

}
