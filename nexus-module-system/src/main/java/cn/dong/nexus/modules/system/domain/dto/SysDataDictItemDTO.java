package cn.dong.nexus.modules.system.domain.dto;

import cn.dong.nexus.core.api.ApiMessage;
import cn.dong.nexus.core.base.BaseDTO;
import cn.dong.nexus.core.base.BaseEntity;
import cn.dong.nexus.core.exception.BizException;
import cn.dong.nexus.modules.system.domain.entity.SysDataDict;
import cn.dong.nexus.modules.system.domain.entity.SysDataDictItem;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "数据字典项 DTO")
public class SysDataDictItemDTO extends BaseDTO<SysDataDictItem> {

    @NotBlank
    @Schema(description = "字典 ID")
    private String dataDictId;

    @NotBlank
    @Schema(description = "字典项名称")
    private String text;

    @NotBlank
    @Schema(description = "字典项值")
    private String value;

    @NotNull
    @Schema(description = "排序")
    private Integer sort;

    @Override
    public void doValidate() {
        super.doValidate();
        boolean exists = Db.lambdaQuery(SysDataDict.class)
                .eq(BaseEntity::getId, dataDictId)
                .exists();
        if (!exists) {
            throw new BizException(ApiMessage.NOT_FOUND);
        }
        exists = Db.lambdaQuery(SysDataDictItem.class)
                .eq(SysDataDictItem::getDataDictId, dataDictId)
                .eq(SysDataDictItem::getText, text)
                .ne(isUpdate(), SysDataDictItem::getId, getId())
                .exists();
        if (exists) {
            throw new BizException("字典项名称已存在！");
        }
        exists = Db.lambdaQuery(SysDataDictItem.class)
                .eq(SysDataDictItem::getDataDictId, dataDictId)
                .eq(SysDataDictItem::getValue, value)
                .ne(isUpdate(), SysDataDictItem::getId, getId())
                .exists();
        if (exists) {
            throw new BizException("字典项值已存在！");
        }
    }
}
