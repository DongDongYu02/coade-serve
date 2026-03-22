package cn.dong.nexus.modules.system.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_attachment")
public class SysAttachment {

    private String id;

    private String name;

    private String originName;

    private String path;

    private Long size;

    private String mime;

    private String ownerId;

    private String ownerType;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT)
    private String createBy;
}
