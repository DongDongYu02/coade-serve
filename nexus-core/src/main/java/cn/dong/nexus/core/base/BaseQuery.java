package cn.dong.nexus.core.base;

import cn.dong.nexus.core.annotations.Query;
import cn.dong.nexus.core.annotations.SelectColumns;
import cn.dong.nexus.core.exception.BizException;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

/**
 * <p> 基础条件查询对象 </p>
 * <p> 配合@Query注解实现条件组装 </p>
 *
 * @author Dong
 **/
@Data
public class BaseQuery<T> {

    public QueryWrapper<T> toQueryWrapper() {
        Class<?> clazz = this.getClass();
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        Field[] fields = ReflectUtil.getFields(clazz);
        if (ArrayUtil.isEmpty(fields)) {
            return queryWrapper;
        }

        // 组装条件
        for (Field field : fields) {
            Object condition = ReflectUtil.getFieldValue(this, field);
            Query query = field.getAnnotation(Query.class);
            if (query == null) {
                continue;
            }
            SqlKeyword sqlKeyword = query.value();
            String queryField = getQueryField(query, field);

            addCondition(queryWrapper, sqlKeyword, queryField, condition);
        }

        // 组装查询字段
        SelectColumns selectColumns = clazz.getAnnotation(SelectColumns.class);
        if (Objects.nonNull(selectColumns)) {
            String[] columns = selectColumns.columns();
            if (ArrayUtil.isNotEmpty(columns)) {
                queryWrapper.select(columns);
            }
        }
        return queryWrapper;
    }


    private void addCondition(QueryWrapper<?> queryWrapper,
                              SqlKeyword sqlKeyword,
                              String fieldName,
                              Object condition) {
        boolean notEmpty = ObjectUtil.isNotEmpty(condition);
        switch (sqlKeyword) {
            case EQ:
                queryWrapper.eq(notEmpty, fieldName, condition);
                break;
            case LIKE:
                queryWrapper.like(notEmpty, fieldName, condition);
                break;
            case GE:
                queryWrapper.ge(notEmpty, fieldName, condition);
                break;
            case LE:
                queryWrapper.le(notEmpty, fieldName, condition);
                break;
            case IN:
                queryWrapper.in(notEmpty, fieldName, parseInCondition(condition));
                break;
            case ASC:
                queryWrapper.orderByAsc(fieldName);
                break;
            case DESC:
                queryWrapper.orderByDesc(fieldName);
                break;
        }

    }

    /**
     * <p>解析IN条件的值</p>
     * <p>IN条件的值必须为String类型，且以“,”分割</p>
     *
     * @param condition 条件值
     **/
    private List<String> parseInCondition(Object condition) {
        if (Objects.isNull(condition)) {
            return null;
        }
        try {
            if (!(condition instanceof String)) {
                throw new IllegalArgumentException();
            }
            return StrUtil.split((String) condition, StrUtil.COMMA);
        } catch (Exception e) {
            throw new BizException("IN condition must be String type and split by “,”");
        }

    }

    private String getQueryField(Query query, Field field) {
        String fieldName = query.column();
        return StrUtil.isBlank(fieldName) ?
                StrUtil.toUnderlineCase(field.getName()) : StrUtil.toUnderlineCase(fieldName);
    }

}
