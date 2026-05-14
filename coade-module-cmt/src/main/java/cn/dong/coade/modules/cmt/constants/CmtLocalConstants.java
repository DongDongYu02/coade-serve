package cn.dong.coade.modules.cmt.constants;

import java.util.Map;
import java.util.Set;

/**
 * CMT 局部常量
 */
public interface CmtLocalConstants {

    String[] USER_BASIC_PERMISSIONS = {"leave", "outgoing", "biz-trip", "attend"};
    Integer ATTEND_RULE_IMD_WECOM_ID = 13;


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


    interface ISSUE_DEMAND_STATUS {
        Integer PENDING = 0;
        /**
         * 评估中
         */
        Integer ASSESSING = 1;
        /**
         * 开发中
         */
        Integer IN_PROGRESS = 2;
        /**
         * 完成
         */
        Integer COMPLETED = 3;
        /**
         * 驳回
         */
        Integer REJECTED = 4;
        /**
         * 作废
         */
        Integer VOIDED = 5;
        /**
         * 待验收
         */
        Integer PENDING_ACCEPT = 6;


        Set<Integer> PROCESSING = Set.of(ASSESSING, IN_PROGRESS);

        Set<Integer> DEC_COMPLETED = Set.of(COMPLETED, PENDING_ACCEPT);
    }

    interface ISSUE_DEMAND_TYPE {
        Integer ISSUE = 1;
        Integer DEMAND = 2;
    }

    interface LEAVE_REQUEST_TYPE {
        Integer PERSONAL = 2;
        Integer SICK = 3;

        String PERSONAL_TEXT = "事假";
        String SICK_TEXT = "病假";

        Map<Integer, String> DICT_MAP = Map.of(
                PERSONAL, PERSONAL_TEXT,
                SICK, SICK_TEXT
        );
    }

    interface ATTEND_REQUEST_STATUS {
        Integer PENDING = 0;
        Integer APPROVED = 1;
        Integer REJECTED = 2;
        Integer REVOKED = 3;

    }

    interface LEAVE_BIZ_TYPE {
        /**
         * 假勤类型 请假
         */
        Integer LEAVE = 5;

        /**
         * 假勤类型 加班
         */
        Integer OVERTIME = 6;

        /**
         * 假勤类型 外出
         */
        Integer OUTGOING = 7;

        /**
         * 假勤类型 出差
         */
        Integer BIZ_TRIP = 4;
    }

    interface ANALYSIS_TIME_RANGE {
        int THIS_YEAR = 1;
        int THIS_QUARTER = 2;
        int THIS_MONTH = 3;
        int THIS_WEEK = 4;
        int LAST_QUARTER = 5;
        int LAST_MONTH = 6;
        int LAST_WEEK = 7;
    }

}
