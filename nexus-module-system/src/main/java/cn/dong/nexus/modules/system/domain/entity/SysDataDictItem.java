package cn.dong.nexus.modules.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_data_dict_item")
public class SysDataDictItem {

    private String id;

    private String text;

    private String value;

    private String dataDictId;

    private Integer sort;

}
