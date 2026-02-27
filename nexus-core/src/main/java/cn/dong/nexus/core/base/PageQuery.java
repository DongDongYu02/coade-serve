package cn.dong.nexus.core.base;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "分页条件请求实体")
public class PageQuery<T> extends BaseQuery<T> {

    @Schema(description = "页码")
    @TableField(exist = false)
    private Integer pageNo = 1;

    @Schema(description = "单页条数")
    @TableField(exist = false)
    private Integer pageSize = 10;

    public Page<T> toPage() {
        return new Page<>(pageNo, pageSize);
    }

    public void selectAllData() {
        this.pageNo = 1;
        this.pageSize = -1;
    }

}
