package cn.dong.nexus.infra.util;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import org.springframework.util.Assert;

import java.util.Objects;
import java.util.function.Supplier;

public class DynamicDataSourceUtil {

    private DynamicDataSourceUtil() {
    }

    /**
     * 切换到指定数据源执行，并返回结果
     */
    public static <T> T switchTo(String dataSource, Supplier<T> action) {
        Assert.hasText(dataSource, "dataSource must not be blank");
        Objects.requireNonNull(action, "action must not be null");

        try {
            DynamicDataSourceContextHolder.push(dataSource);
            return action.get();
        } finally {
            DynamicDataSourceContextHolder.poll();
        }
    }

    /**
     * 切换到指定数据源执行，无返回值
     */
    public static void switchTo(String dataSource, Runnable action) {
        Assert.hasText(dataSource, "dataSource must not be blank");
        Objects.requireNonNull(action, "action must not be null");

        try {
            DynamicDataSourceContextHolder.push(dataSource);
            action.run();
        } finally {
            DynamicDataSourceContextHolder.poll();
        }
    }

    /**
     * 支持抛受检异常的版本
     */
    public static <T> T switchToChecked(String dataSource, ThrowingSupplier<T> action) {
        Assert.hasText(dataSource, "dataSource must not be blank");
        Objects.requireNonNull(action, "action must not be null");

        try {
            DynamicDataSourceContextHolder.push(dataSource);
            return action.get();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("执行动态数据源操作失败, dataSource=" + dataSource, e);
        } finally {
            DynamicDataSourceContextHolder.poll();
        }
    }

    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Exception;
    }
}
