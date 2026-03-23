package cn.dong.coade.modules.cmt.constants;

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
    }


}
