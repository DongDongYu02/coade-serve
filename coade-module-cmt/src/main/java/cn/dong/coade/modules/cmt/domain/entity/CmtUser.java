package cn.dong.coade.modules.cmt.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("cmt_user")
public class CmtUser {

    private String id;

    private String username;

    private String weComId;

    private String ekpId;

    private Integer status;

    private Integer identity;

    private String avatar;

    private String phone;

    private String dept;

    private String deptId;

}
