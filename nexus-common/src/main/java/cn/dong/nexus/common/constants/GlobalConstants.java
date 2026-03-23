package cn.dong.nexus.common.constants;

import cn.hutool.core.lang.tree.TreeNodeConfig;

public interface GlobalConstants {
    Integer INT_ZERO = 0;

    Integer INT_ONE = 1;

    Integer INT_YES = INT_ONE;

    Integer INT_NO = INT_ZERO;

    /**
     * 时间格式
     **/
    interface DatePattern {

        String NORMAL_ONLY_DATE = "yyyy-MM-dd";

        String NORMAL = "yyyy-MM-dd HH:mm:ss";

        String Y_M_D_H_M = "yyyy-MM-dd HH:mm";

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

    interface DataSource {
        /**
         * 本地 MySQL 数据源
         */
        String LOCAL_MYSQL = "local-mysql";
        /**
         * EKP SQL Server 数据源
         */
        String EKP_SQLSERVER = "ekp-sqlserver";
    }

    interface UserIdentity {
        /**
         * 用户身份 管理员
         */
        Integer ADMIN = 1;
        /**
         * 用户身份 普通用户
         */
        Integer NORMAL = 2;
        /**
         * 用户身份 特殊用户
         */
        Integer SPECIAL = 3;
    }

    interface EkpLeaveType {
        /**
         * 假勤类型 请假
         */
        Integer LEAVE = 5;

        /**
         * 假勤类型 外出
         */
        Integer OUTGOING = 7;

        /**
         * 假勤类型 出差
         */
        Integer BIZ_TRIP = 4;
    }

    interface CacheKey {
        String EKP_PROVIDE_TOKEN = "ekp_provide_token:";
    }

    interface AttendReissueApprovalResult {

        /**
         * 1-审批通过
         */
        Integer APPROVED = 1;

        /**
         * 2-审批驳回
         */
        Integer REJECTED = 2;
    }

    interface TableName {
        String CMT_6S_REVIEW = "cmt_6s_review";
        String CMT_DEPT = "cmt_department";
        String CMT_USER = "cmt_user";
        String CMT_PERMISSION = "cmt_permission";
        String SYS_DATA_DICT_ITEM = "sys_data_dict_item";
    }
}
