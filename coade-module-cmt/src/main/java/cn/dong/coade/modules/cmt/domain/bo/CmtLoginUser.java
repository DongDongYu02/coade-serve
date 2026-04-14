package cn.dong.coade.modules.cmt.domain.bo;

import cn.dong.nexus.core.security.context.LoginUser;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CmtLoginUser extends LoginUser {

    private String weComId;

    private String dept;

    private String ekpId;
}
