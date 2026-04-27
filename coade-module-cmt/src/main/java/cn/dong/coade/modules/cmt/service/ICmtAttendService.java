package cn.dong.coade.modules.cmt.service;

import cn.dong.coade.modules.cmt.domain.bo.AttendDurationBO;
import cn.dong.coade.modules.cmt.domain.dto.*;
import cn.dong.coade.modules.cmt.domain.query.AttendMonthDataQuery;
import cn.dong.coade.modules.cmt.domain.query.AttendOutgoingDurationQuery;
import cn.dong.coade.modules.cmt.domain.query.AttendOvertimeDurationQuery;
import cn.dong.coade.modules.cmt.domain.vo.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ICmtAttendService {

    /**
     * 获取用户今日考勤信息
     */
    UserAttendInfoVO getUserTodayAttend();

    UserAttendInfoVO getUserAttendByDate(int year, int month, int day);

    AttendDurationVO getCurrentUserLeaveDuration(LocalDateTime beginTime, LocalDateTime endTime);

    AttendDurationVO getAttendDurationByEkpUserId(String ekpUserId, LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 计算考勤时长
     */
    AttendDurationBO calculateDurationOfAttend(String weComId, LocalDateTime beginTime, LocalDateTime endTime);

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

    /**
     * 用户提交请假申请
     */
    void addLeaveRequest(AttendLeaveRequestDTO dto);

    /**
     * EKP回调更新请假审批状态或新增请假申请
     */
    void saveOrUpdateLeaveRequestStatus(AttendLeaveRequestEkpCallbackDTO dto);

    /**
     * 获取用户请假申请记录
     *
     */
    List<AttendLeaveRequestVO> getUserLeaveRequestList();

    /**
     * 撤销请假申请
     *
     */
    void revokeLeaveRequest(String id);

    /**
     * 查询用户外出工时
     */
    AttendDurationVO getOutgoingDurationByEkpUserId(AttendOutgoingDurationQuery query);

    /**
     * 新增外出申请
     */
    void addOutgoingRequest(AttendOutgoingRequestDTO dto);

    /**
     * EKP回调更新外出审批状态或新增请假申请
     */
    void saveOrUpdateOutgoingRequestStatus(AttendOutgoingRequestEkpCallbackDTO dto);

    /**
     * 获取当前用户外出期间的有效工时
     */
    BigDecimal getCurrentUserOutgoingDuration(AttendOutgoingDurationQuery query);

    /**
     * 获取用户外出申请记录
     */
    List<AttendOutgoingRequestVO> getUserOutgoingRequestList();

    /**
     * 撤销请假申请
     */
    void revokeOutgoingRequest(String id);

    /**
     * 提交出差申请
     */
    void addBizTripRequest(AttendBizTripRequestDTO dto);

    /**
     * EKP回调更新出差审批状态或新增出差申请
     */
    void saveOrUpdateBizTripRequestStatus(AttendBizTripRequestEkpCallbackDTO dto);

    /**
     * 获取用户出差申请记录
     */
    List<AttendBizTripRequestVO> getUserBizTripRequestList();

    /**
     * 撤销出差申请
     */
    void revokeBizTripRequest(String id);

    /**
     * 提交加班申请
     */
    void addOvertimeRequest(AttendOvertimeRequestDTO dto);

    /**
     * EKP查询用户加班时长
     */
    BigDecimal getOvertimeDurationByEkpUserId(AttendOvertimeDurationQuery query);

    /**
     * EKP回调更新加班审批状态或新增加班申请
     */
    void saveOrUpdateOvertimeRequestStatus(AttendOvertimeRequestEkpCallbackDTO dto);

    /**
     * 撤销加班申请
     */
    void revokeOvertimeRequest(String id);

    /**
     * 获取当前用户的加班时长
     */
    BigDecimal getCurrentUserOvertimeDuration(AttendOvertimeDurationQuery query);

    /**
     * 用户加班申请列表
     */
    List<AttendOvertimeRequestVO> getUserOvertimeRequestList();


    /**
     * 查询用户月考勤数据
     */
    List<AttendMonthDataVO> getUserMonthAttendData(AttendMonthDataQuery query);
}
