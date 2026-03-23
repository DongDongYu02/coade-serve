package cn.dong.coade.modules.cmt.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("cmt_permission")
public class CmtPermission {

    private String id;

    private String name;

    private String code;

    private String isBasic;
}
