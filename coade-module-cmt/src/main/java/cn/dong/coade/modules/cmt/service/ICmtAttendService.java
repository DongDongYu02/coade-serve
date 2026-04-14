package cn.dong.coade.modules.cmt.service;

import cn.dong.coade.modules.cmt.domain.dto.AttendReissueApplyPassDTO;
import cn.dong.coade.modules.cmt.domain.dto.ReissueAttendDTO;
import cn.dong.coade.modules.cmt.domain.vo.UserAttendInfoVO;
import cn.dong.coade.modules.cmt.domain.vo.UserAttendRecordVO;

import java.util.List;

public interface ICmtAttendService {

    /**
     * 获取用户今日考勤信息
     */
    UserAttendInfoVO getUserTodayAttend();

    UserAttendInfoVO getUserAttendByDate(int year, int month, int day);

    /**
     * 获取用户的考勤规则
     */
    String getUserAttendRule(String ekpId);

    /**
     * 发起蓝凌补卡申请流程
     */
    void reissueAttendApply(ReissueAttendDTO dto);

    /**
     * 为登录用户发起蓝凌补卡申请流程
     */
    void reissueAttendApplyForLoginUser(ReissueAttendDTO dto);

    /**
     * 补卡申请通过 蓝凌回调
     */
    void doReissueAttend(AttendReissueApplyPassDTO dto);

    /**
     * 获取用户该月补卡已使用次数
     */
    Integer getUsedReissueFrequency(String cmtUserId, Integer year, Integer month);

    /**
     * 获取用户月考勤异常记录
     */
    List<UserAttendRecordVO> getMonthAbnormal(Integer month);
}
