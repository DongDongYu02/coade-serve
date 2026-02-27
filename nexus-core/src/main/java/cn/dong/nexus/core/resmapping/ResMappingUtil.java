package cn.dong.nexus.core.resmapping;

import cn.dong.nexus.core.resmapping.annotation.ResMapping;
import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Singleton;
import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.support.LambdaMeta;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Slf4j
public class ResMappingUtil {

    public static final ResMappingHandler RES_MAPPING_HANDLER = Singleton.get(ResMappingHandler.class);

    /**
     * 获取资源字段映射
     *
     * @param keyFiledGetter 资源key
     * @param valFiledGetter 资源val
     * @author Dong
     * @date 16:08 2023/10/12
     **/
    public static <K, F, V> Map<F, V> getFieldMapping(SFunction<K, F> keyFiledGetter,
                                                      SFunction<K, V> valFiledGetter) {
        return getFieldMapping(null, keyFiledGetter, valFiledGetter, false);
    }

    public static <K, F, V> Map<F, V> getFieldMapping(SFunction<K, F> keyFiledGetter,
                                                      SFunction<K, V> valFiledGetter,
                                                      boolean useLogicDel) {
        return getFieldMapping(null, keyFiledGetter, valFiledGetter, useLogicDel);
    }

    /**
     * 获取资源字段映射
     *
     * @param keys           资源key
     * @param keyFiledGetter 资源key
     * @param valFiledGetter 资源val
     * @author Dong
     * @date 16:09 2023/10/12
     **/
    public static <K, F, V> Map<F, V> getFieldMapping(List<F> keys,
                                                      SFunction<K, F> keyFiledGetter,
                                                      SFunction<K, V> valFiledGetter,
                                                      boolean useLogicDel) {
        // 解析字段方法引用
        LambdaMeta extract = LambdaUtils.extract(keyFiledGetter);
        String keyField = extract.getImplMethodName().replace("get", "").toLowerCase();
        extract = LambdaUtils.extract(valFiledGetter);
        String valField = extract.getImplMethodName().replace("get", "").toLowerCase();


        // 获取方法引用源类型
        Class<K> clazz = (Class<K>) extract.getInstantiatedClass();

        // ORM实体上必须有@TableName注解
        Object tableName = AnnotationUtil.getAnnotationValue(clazz, TableName.class);
        Assert.notNull(tableName, "annotation [TableName] value is required");


        // 先从缓存获取，若没有则查询DB
        Map<F, V> cache = RES_MAPPING_HANDLER.getResourceByCache(keys, String.valueOf(tableName), keyField, valField);
        if (Objects.nonNull(cache)) {
            return cache;
        }

        /* 构建一个QueryWrapper用于获取SQL语句*/
        LambdaQueryWrapper<K> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(keyFiledGetter, valFiledGetter).in(CollUtil.isNotEmpty(keys), keyFiledGetter, keys);

        // 执行sql查询
        String sql = RES_MAPPING_HANDLER.parseSql(queryWrapper, String.valueOf(tableName), useLogicDel);
        List<K> result = RES_MAPPING_HANDLER.executeQuerySql(sql, clazz);

        if (result.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<F, V> resultMap = CollUtil.isEmpty(result) ? Collections.emptyMap() :
                result.stream()
                        .filter(item -> Objects.nonNull(valFiledGetter.apply(item))
                                        && Objects.nonNull(keyFiledGetter.apply(item)))
                        .collect(Collectors.toMap(keyFiledGetter, valFiledGetter));
        RES_MAPPING_HANDLER.setResourceCache(keys, String.valueOf(tableName), keyField, valField, resultMap);
        return resultMap;
    }

    public static <K, F, V> Map<F, V> getFieldMapping(List<F> keys,
                                                      SFunction<K, F> keyFiledGetter,
                                                      SFunction<K, V> valFiledGetter) {
        return getFieldMapping(keys, keyFiledGetter, valFiledGetter, false);
    }


    public static <K, F, V> Map<F, V> getFieldMapping(List<F> keys,
                                                      Field keyFiledGetter,
                                                      Field valFiledGetter,
                                                      boolean useLogicDel) {
        String keyField = keyFiledGetter.getName();
        String valField = valFiledGetter.getName();

        // ORM实体上必须有@TableName注解
        Object[] tableInfo = RES_MAPPING_HANDLER.translateVerify(keyField, keyFiledGetter, valFiledGetter);
        Class<K> clazz = (Class<K>) tableInfo[0];
        Object tableName = tableInfo[1];

        // 先从缓存获取，若没有则查询DB
        Map<F, V> cache = RES_MAPPING_HANDLER.getResourceByCache(keys, String.valueOf(tableName), keyField, valField);
        if (Objects.nonNull(cache)) {
            return cache;
        }

        /* 构建一个QueryWrapper用于获取SQL语句*/
        QueryWrapper<K> queryWrapper = new QueryWrapper<>();
        queryWrapper.select(keyField, valField).in(CollUtil.isNotEmpty(keys), keyField, keys);

        // 执行sql查询
        String sql = RES_MAPPING_HANDLER.parseSql(queryWrapper, String.valueOf(tableName), useLogicDel);
        List<K> result = RES_MAPPING_HANDLER.executeQuerySql(sql, clazz);

        if (result.isEmpty()) {
            return Collections.emptyMap();
        }
        SFunction<Object, F> keyGetterSFun = RES_MAPPING_HANDLER.toFieldGetterSFunction(keyFiledGetter);
        SFunction<Object, V> valGetterSFun = RES_MAPPING_HANDLER.toFieldGetterSFunction(valFiledGetter);
        Map<F, V> resultMap = CollUtil.isEmpty(result) ? Collections.emptyMap() :
                result.stream()
                        .filter(item -> Objects.nonNull(keyGetterSFun.apply(item))
                                        && Objects.nonNull(valGetterSFun.apply(item)))
                        .collect(Collectors.toMap(keyGetterSFun, valGetterSFun));
        RES_MAPPING_HANDLER.setResourceCache(keys, String.valueOf(tableName), keyField, valField, resultMap);
        return resultMap;
    }


    public static <T, R, V> void translateField(List<T> data, SFunction<T, R> keyMapper,
                                                Field sourceKeyField,
                                                Field sourceValField,
                                                BiConsumer<T, V> valFieldSetter) {
        if (CollUtil.isEmpty(data)) {
            return;
        }
        List<R> keys = data.stream().map(keyMapper).distinct().collect(Collectors.toList());
        Map<R, V> res = getFieldMapping(keys, sourceKeyField, sourceValField, false);
        RES_MAPPING_HANDLER.doCollectApply(res, data, keyMapper, valFieldSetter);
    }

    public static <T, R, V, S> void translateField(List<T> data, SFunction<T, R> keyMapper,
                                                   SFunction<S, R> keyFiledGetter,
                                                   SFunction<S, V> valFiledGetter,
                                                   BiConsumer<T, V> valFieldSetter) {
        if (CollUtil.isEmpty(data)) {
            return;
        }
        List<R> keys = data.stream().map(keyMapper).distinct().collect(Collectors.toList());
        Map<R, V> res = getFieldMapping(keys, keyFiledGetter, valFiledGetter);
        RES_MAPPING_HANDLER.doCollectApply(res, data, keyMapper, valFieldSetter);
    }


    /**
     * <p>获取加了ResourceMapping注解的字段</p>
     * <p>根据注解内容进行翻译</p>
     *
     * @param data 需要翻译的数据
     * @author Dong
     * @date 17:42 2023/11/6
     **/
    public static <T, R, V, S> void translateField(List<T> data) {
        if (CollUtil.isEmpty(data)) {
            return;
        }
        // 获取数据类型
        Class<T> elementClass = RES_MAPPING_HANDLER.getElementClass(data);
        if (Objects.isNull(elementClass)) {
            return;
        }
        // 遍历类型字段若有@ResourceMapping注解则进行翻译
        Field[] fields = ReflectUtil.getFields(elementClass);
        for (Field field : fields) {
            ResMapping annotation = field.getAnnotation(ResMapping.class);
            if (Objects.isNull(annotation)) {
                continue;
            }
            String[] targetFieldName = annotation.targets();
            String sourceTable = annotation.sourceTable();
            String key = annotation.key();
            String[] values = annotation.values();

            // 资源类型
            Class<S> sourceClass = RES_MAPPING_HANDLER.getSourceClass(sourceTable);
            if (Objects.isNull(sourceClass)) {
                continue;
            }
            // 目标字段
            List<Field> targetFields = RES_MAPPING_HANDLER.getTargetField(field, elementClass, targetFieldName);
            // 资源key
            Field sourceKeyField = RES_MAPPING_HANDLER.getSourceKeyField(key, sourceClass);
            // 资源values
            List<Field> sourceValFields = RES_MAPPING_HANDLER.getSourceValField(values, sourceClass);
            if (targetFields.isEmpty() || sourceValFields.isEmpty()) {
                continue;
            }
            for (int i = 0; i < targetFields.size(); i++) {
                Field target = targetFields.get(i);
                Field sourceVal = sourceValFields.get(i);
                SFunction<T, R> keyMapper = RES_MAPPING_HANDLER.toFieldGetterSFunction(field);
                BiConsumer<T, V> valFieldSetter = RES_MAPPING_HANDLER.toFieldSetterBiConsumer(target);
                translateField(data, keyMapper, sourceKeyField, sourceVal, valFieldSetter);
            }
        }

    }

    public static <T> void translateObjField(T data) {
        List<T> single = List.of(data);
        translateField(single);
    }

}
