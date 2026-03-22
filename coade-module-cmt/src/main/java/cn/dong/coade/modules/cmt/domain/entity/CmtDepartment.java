package cn.dong.coade.modules.cmt.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("cmt_department")
public class CmtDepartment {

    private String id;

    private String name;

    private String ekpOrgId;

    private String ekpNo;

    private LocalDateTime updateTime;

}
