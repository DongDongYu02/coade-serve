package cn.dong.nexus.modules.rbac.domain.vo.detail;


import cn.dong.nexus.core.base.BaseDetailVO;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;


@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "用户详情 VO")
public class SysUserDetailVO extends BaseDetailVO {

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "性别 1男 2女")
    private Integer sex;

    @Schema(description = "状态 1正常 2冻结")
    private Integer status;

    @Schema(description = "角色 ID")
    private List<String> roleIds;

    @Schema(description = "角色名称")
    private List<String> roleNames;


}