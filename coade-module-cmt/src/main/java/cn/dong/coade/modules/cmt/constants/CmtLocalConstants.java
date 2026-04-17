package cn.dong.coade.modules.cmt.constants;

import java.util.Map;

/**
 * CMT 局部常量
 */
public interface CmtLocalConstants {

    String[] USER_BASIC_PERMISSIONS = {"leave", "outgoing", "biz-trip", "attend"};

    /**
     * 6S评审状态
     */
    interface _6S_REVIEW_STATUS {
        /**
         * 分析中
         */
        Integer IN_ANALYSIS = 0;
        /**
         * 分析完成
         */
        Integer ANALYSIS_COMPLETED = 1;
        /**
         * 待整改
         */
        Integer PENDING_RECTIFY = 2;
        /**
         * 已完成
         */
        Integer COMPLETED = 3;
        /**
         * 分析失败
         */
        Integer ANALYSIS_FAILED = 4;

        Map<Integer, String> DICT_MAP = Map.of(
                IN_ANALYSIS, "分析中",
                ANALYSIS_COMPLETED, "分析完成",
                PENDING_RECTIFY, "待整改",
                COMPLETED, "已完成",
                ANALYSIS_FAILED, "分析失败"
        );
    }


    Integer ATTEND_RULE_IMD_WECOM_ID = 13;


    interface ISSUE_DEMAND_STATUS {
        Integer PENDING = 0;
        Integer ACCEPTED = 1;
        Integer IN_PROGRESS = 2;
        Integer COMPLETED = 3;
        Integer REJECTED = 4;
        Integer CLOSED = 5;
    }

    interface ISSUE_DEMAND_TYPE {
        Integer ISSUE = 1;
        Integer DEMAND = 2;
    }


}
