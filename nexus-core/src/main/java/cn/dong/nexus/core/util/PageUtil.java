package cn.dong.nexus.core.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * MP分页对象工具类
 *
 * @author Dong
 * @date 11:14 2023/11/8
 **/
public class PageUtil{

    private static final IPage<?> EMPTY_PAGE = new Page<>();

    /**
     * 分页对象转换
     *
     * @param data  分页对象
     * @param clazz 转换对象类型
     * @author Dong
     * @date 11:14 2023/11/8
     **/
    public static <E, T> IPage<T> convertPage(IPage<E> data, Class<T> clazz) {
        return data.convert(item -> BeanUtil.copyProperties(item, clazz));
    }

    public static <E, T> IPage<T> convertPage(IPage<E> data, Class<T> clazz, CopyOptions copyOptions) {
        return data.convert(item -> {
            T obj = ReflectUtil.newInstance(clazz);
            BeanUtil.copyProperties(item, obj, copyOptions);
            return obj;
        });
    }


    public static <T> IPage<T> emptyPage() {
        return (IPage<T>) EMPTY_PAGE;
    }

}
