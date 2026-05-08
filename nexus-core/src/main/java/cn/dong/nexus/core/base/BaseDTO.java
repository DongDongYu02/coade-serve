package cn.dong.nexus.core.base;

import cn.dong.nexus.core.api.ApiMessage;
import cn.dong.nexus.core.exception.BizException;
import cn.dong.nexus.core.valid.BizValidate;
import cn.dong.nexus.core.valid.ValidGroup;
import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

@Data
@Accessors(chain = true)
@SuppressWarnings("unchecked")
public class BaseDTO<T> {
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Class<T> thisGeneric;

    {
        Type actualTypeArgument = ((ParameterizedType) this.getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
        thisGeneric = (Class<T>) actualTypeArgument;
    }

    @Schema(description = "ID")
    @NotBlank(message = "ID 不能为空", groups = ValidGroup.Update.class)
    private String id;

    public T toEntity() {
        return BeanUtil.copyProperties(this, thisGeneric);
    }

    @Schema(hidden = true)
    public boolean isUpdate() {
        return Objects.nonNull(id) && StrUtil.isNotBlank(id);
    }

    public void checkExists() {
        boolean exists = Db.query(thisGeneric).eq("id", this.id).exists();
        if (!exists) {
            throw new BizException(ApiMessage.NOT_FOUND);
        }
    }

    public void doValidate() {
        if (isUpdate()) {
            checkExists();
        }
        Field[] fields = ReflectUtil.getFields(this.getClass());
        for (Field field : fields) {
            Object fieldValue = ReflectUtil.getFieldValue(this, field);
            if (Objects.isNull(fieldValue) || (fieldValue instanceof String str && StrUtil.isBlank(str))) {
                continue;
            }
            BizValidate.Unique anno = AnnotationUtil.getAnnotation(field, BizValidate.Unique.class);
            if (Objects.isNull(anno)) {
                continue;
            }
            Object annoColumn = AnnotationUtil.getAnnotationValue(field, BizValidate.Unique.class, "column");
            String column = StrUtil.isBlank((String) annoColumn) ? StrUtil.toUnderlineCase(field.getName()) : (String) annoColumn;
            Long count = Db.query(thisGeneric).eq(column, fieldValue)
                    .ne(this.isUpdate(), "id", this.id)
                    .count();
            if (count > 0) {
                throw new BizException(anno.message());
            }
        }
    }

}
