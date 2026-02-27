package cn.dong.nexus.common.constants;

import cn.hutool.core.lang.tree.TreeNodeConfig;

public interface GlobalConstants {
    Integer INT_ZERO = 0;

    Integer INT_ONE = 1;

    /**
     * 时间格式
     **/
    interface DatePattern {

        String NORMAL_ONLY_DATE = "yyyy-MM-dd";

        String NORMAL = "yyyy-MM-dd HH:mm:ss";

    }

    /**
     * 时区
     **/
    interface ZoneTime {

        String GMT8 = "GMT+8";
    }

    interface ENABLE_STATUS {
        Integer ENABLED = 1;
        Integer DISABLED = 0;
    }

    /**
     * 权限表根节点 ID
     */
    String ROOT_ID = "0";

    TreeNodeConfig TREE_NODE_CONFIG = new TreeNodeConfig().setIdKey("id").setWeightKey("sort");

    interface PERMISSION_TYPE {
        /**
         * 目录
         */
        Integer CATALOG = 1;
        /**
         * 菜单
         */
        Integer MENU = 2;
        /**
         * 按钮
         */
        Integer ACTION = 3;
    }


    interface ConfigGroup {
        /**
         * 系统设置
         */
        Integer SETTING = 1;
    }

    interface ConfigKey {
        /**
         * 系统名称
         */
        String SYS_NAME = "systemName";
        /**
         * 系统 LOGO
         */
        String SYS_LOGO = "systemLogo";
    }
}
