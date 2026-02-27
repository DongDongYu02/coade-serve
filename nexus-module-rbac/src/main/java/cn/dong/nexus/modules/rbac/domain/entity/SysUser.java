package cn.dong.nexus.modules.rbac.domain.entity;

import cn.dong.nexus.core.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    private String username;

    private String password;

    private String nickname;

    private String phone;

    private Integer sex;

    private Integer status;

    private Integer identity;

    private String avatar;

}
