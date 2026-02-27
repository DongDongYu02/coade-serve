package cn.dong.nexus.core.resmapping;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.xml.bind.DatatypeConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionUtils;

import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.Duration;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@SuppressWarnings("unchecked")
public class ResMappingHandler {

    private ResMappingHandler() {
    }

    private static final String DEFAULT_TARGET_KEY_SUFFIX = "Id";

    private static final String DEFAULT_TARGET_VAL_SUFFIX = "Name";

    public static final Cache<Object, Object> RESOURCE_CACHE = Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterAccess(Duration.ofMinutes(5))
            .build();

    /**
     * 使用SqlSession进行原生JDBC查询SQL
     *
     * @param sql   sql语句
     * @param clazz 查询的对象类型
     * @author Dong
     * @date 10:14 2023/11/3
     **/
    public <T> List<T> executeQuerySql(String sql, Class<T> clazz) {
        SqlSession session = SqlHelper.sqlSession(clazz);
        List<T> result = new ArrayList<>();
        PreparedStatement pst = null;
        ResultSet resultSet;
        try {
            pst = session.getConnection().prepareStatement(sql);
            resultSet = pst.executeQuery();
            ResultSetMetaData md = resultSet.getMetaData();
            int columnCount = md.getColumnCount();
            while (resultSet.next()) {
                Map<String, Object> rowData = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    rowData.put(md.getColumnName(i), resultSet.getObject(i));
                }
                T obj = ReflectUtil.newInstance(clazz);
                BeanUtil.fillBeanWithMap(rowData, obj, true);
                result.add(obj);
            }
        } catch (SQLException e) {
            log.error("SQL异常:{},{}", sql, e.getMessage());
        } finally {
            IoUtil.close(pst);
            SqlSessionUtils.closeSqlSession(session, GlobalConfigUtils.currentSessionFactory(clazz));
        }
        return result;

    }

    public String parseSql(QueryWrapper<?> queryWrapper, String tableName, boolean useLogicDel) {
        String sqlSelect = queryWrapper.getSqlSelect();
        String customSqlSegment = queryWrapper.getCustomSqlSegment();
        Map<String, Object> paramNameValuePairs = queryWrapper.getParamNameValuePairs();

        return parseSql(sqlSelect, customSqlSegment, paramNameValuePairs, tableName, useLogicDel);
    }

    public String parseSql(LambdaQueryWrapper<?> queryWrapper, String tableName, boolean useLogicDel) {
        String sqlSelect = queryWrapper.getSqlSelect();
        String customSqlSegment = queryWrapper.getCustomSqlSegment();
        Map<String, Object> paramNameValuePairs = queryWrapper.getParamNameValuePairs();

        return parseSql(sqlSelect, customSqlSegment, paramNameValuePairs, tableName, useLogicDel);
    }

    public String parseSql(String sqlSelect, String customSqlSegment,
                           Map<String, Object> paramNameValuePairs,
                           String tableName, boolean useLogicDel) {
        String[] column = sqlSelect.split(StrUtil.COMMA);
        // 查询的key字段
        String keyColumn = column[0];
        // 将字段转换为下划线命名
        sqlSelect = Arrays.stream(column).map(StrUtil::toUnderlineCase).collect(Collectors.joining(StrUtil.COMMA));
        // 将where条件中的字段转换为下划线命名
        customSqlSegment = customSqlSegment.replace(keyColumn, StrUtil.toUnderlineCase(keyColumn));
        // 解析条件
        Pattern pattern = Pattern.compile("#\\{ew\\.paramNameValuePairs\\.(.+?)\\}");
        Matcher matcher = pattern.matcher(customSqlSegment);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String key = matcher.group(1);
            if (paramNameValuePairs.containsKey(key)) {
                matcher.appendReplacement(sb, "'" + paramNameValuePairs.get(key) + "'");
            }
        }
        matcher.appendTail(sb);
        // 逻辑删除条件处理
        if (useLogicDel) {
            TableInfo tableInfo = TableInfoHelper.getTableInfo(tableName);
            if (sb.isEmpty()) {
                sb.append("WHERE ").append(tableInfo.getLogicDeleteSql(false, true));
            } else {
                sb.append(tableInfo.getLogicDeleteSql(true, true));
            }
        }
        return StrUtil.format("SELECT {} FROM {} {}", sqlSelect, tableName, sb.toString());
    }

    public <R, V, T> void doCollectApply(Map<R, V> res, List<T> data,
                                         SFunction<T, R> keyMapper,
                                         BiConsumer<T, V> valFieldSetter) {
        data.forEach(item -> {
            if (res.containsKey(keyMapper.apply(item))) {
                try {
                    R apply = keyMapper.apply(item);
                    V v = res.get(apply);
                    valFieldSetter.accept(item, v);
                } catch (Exception e) {
                    log.error("映射字段无法正确匹配，source:{}", e.getMessage());
                }
            }
        });
    }


    public Object[] translateVerify(String keyField,
                                    Field keyFiledGetter,
                                    Field valFiledGetter) {
        Class<?> clazz = keyFiledGetter.getDeclaringClass();
        Object tableName = AnnotationUtil.getAnnotationValue(clazz, TableName.class);
        if (Objects.isNull(tableName)) {
            if ("id".equals(keyField)) {
                // 获取方法引用源类型
                clazz = valFiledGetter.getDeclaringClass();
                tableName = AnnotationUtil.getAnnotationValue(clazz, TableName.class);
                Assert.notNull(tableName, "annotation [TableName] value is required");
                return new Object[]{clazz, tableName};
            }
        }
        return new Object[]{clazz, tableName};
    }


    public <F, V> Map<F, V> getResourceByCache(List<F> keys, String valueOf, String keyField, String valField) {
        String resourceCacheKey = getResourceCacheKey(keys);
        String cacheKey = StrUtil.format("translate:{}:{}:{}_{}", valueOf, resourceCacheKey, keyField, valField);
        return (Map<F, V>) RESOURCE_CACHE.getIfPresent(cacheKey);
    }


    /**
     * 缓存资源
     *
     * @param keys         资源keys
     * @param tableName    表名
     * @param keyFieldName 资源key字段名
     * @param valFieldName 资源val字段名
     * @param cache        缓存数据
     * @author Dong
     * @date 11:21 2023/10/10
     **/
    public void setResourceCache(List<?> keys, String tableName,
                                 String keyFieldName, String valFieldName,
                                 Object cache) {
        String resourceCacheKey = getResourceCacheKey(keys);
        String cacheKey = StrUtil.format("translate:{}:{}:{}_{}", tableName, resourceCacheKey, keyFieldName, valFieldName);
        RESOURCE_CACHE.put(cacheKey, cache);
    }

    /**
     * <p>通过SHA256生成资源数据查询的唯一key</p>
     *
     * @author Dong
     * @date 11:27 2023/10/10
     **/
    public String getResourceCacheKey(List<?> list) {
        if (CollUtil.isEmpty(list)) {
            return "all";
        }
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        String listAsString = list.stream().map(String::valueOf).collect(Collectors.joining(","));
        md.update(listAsString.getBytes());
        byte[] digest = md.digest();
        return DatatypeConverter.printHexBinary(digest).toUpperCase();
    }

    public Field getSourceKeyField(String source, Class<?> sourceClass) {
        boolean flag = ArrayUtil.isEmpty(source) || StrUtil.isBlank(source);
        return ReflectUtil.getField(sourceClass, flag ? "id" : source);
    }

    public List<Field> getSourceValField(String[] values, Class<?> sourceClass) {
        List<Field> fields = new ArrayList<>(values.length);
        if (ArrayUtil.isEmpty(values)) {
            fields.add(ReflectUtil.getField(sourceClass, "name"));
            return fields;
        }
        if (values.length == 1) {
            if (StrUtil.isBlank(values[0])) {
                log.error("values element must not be blank！");
                return Collections.emptyList();
            }
            fields.add(ReflectUtil.getField(sourceClass, values[0]));
            return fields;
        }
        for (String value : values) {
            if (StrUtil.isBlank(value)) {
                log.error("values element must not be blank！");
                return Collections.emptyList();
            }
            Field field = ReflectUtil.getField(sourceClass, value);
            if (fields.contains(field)) {
                continue;
            }
            fields.add(field);
        }
        return fields;
    }

    public List<Field> getTargetField(Field keyField, Class<?> clazz, String[] fieldNames) {
        List<Field> fields = new ArrayList<>(fieldNames.length);
        if (ArrayUtil.isEmpty(fieldNames)) {
            String fieldName = StrUtil.removeSuffix(keyField.getName(), DEFAULT_TARGET_KEY_SUFFIX)
                    + DEFAULT_TARGET_VAL_SUFFIX;
            fields.add(ReflectUtil.getField(clazz, fieldName));
            return fields;
        }
        if (fieldNames.length == 1) {
            if (StrUtil.isBlank(fieldNames[0])) {
                log.error("values element must not be blank！");
                return Collections.emptyList();
            }
            fields.add(ReflectUtil.getField(clazz, fieldNames[0]));
            return fields;
        }
        for (String fieldName : fieldNames) {
            if (StrUtil.isBlank(fieldName)) {
                log.error("values element must not be blank！");
                return Collections.emptyList();
            }
            Field field = ReflectUtil.getField(clazz, fieldName);
            if (fields.contains(field)) {
                continue;
            }
            fields.add(field);
        }
        return fields;
    }

    public <S> Class<S> getSourceClass(String sourceTable) {
        TableInfo tableInfo = TableInfoHelper.getTableInfo(sourceTable);
        Class<S> clazz = (Class<S>) tableInfo.getEntityType();
        if (Objects.isNull(clazz)) {
            log.error("未找到资源映射类型:{}", sourceTable);
        }
        return clazz;
    }


    public <T, R> SFunction<T, R> toFieldGetterSFunction(Field field) {
        Function<T, R> function = t -> {
            try {
                field.setAccessible(true);
                return (R) field.get(t);
            } catch (IllegalAccessException e) {
                log.error(e.getMessage());
                return null;
            }
        };
        return function::apply;
    }

    public <T, V> BiConsumer<T, V> toFieldSetterBiConsumer(Field field) {
        return (t, v) -> {
            try {
                field.setAccessible(true);
                field.set(t, v);
            } catch (IllegalAccessException e) {
                log.error(e.getMessage());
            }
        };

    }


    public <T> Class<T> getElementClass(List<T> data) {
        T el = data.get(0);
        if (Objects.isNull(el)) {
            return null;
        }
        return (Class<T>) el.getClass();
    }


}
