package cn.dong.nexus.modules.rbac.domain.vo;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p> 用户信息 VO </p>
 *
 * @author Dong
 * @since 2023-11-29 16:27:20
 **/
@Data
public class SysUserVO {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "创建日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

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