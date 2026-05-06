package cn.dong.coade.modules.cmt.controller;

import cn.dong.coade.modules.cmt.domain.dto.*;
import cn.dong.coade.modules.cmt.domain.query.*;
import cn.dong.coade.modules.cmt.domain.vo.*;
import cn.dong.coade.modules.cmt.service.ICmtAttendService;
import cn.dong.coade.modules.cmt.support.aspect.annotation.EkpCallbackValid;
import cn.dong.nexus.common.domain.vo.FileExportVO;
import cn.dong.nexus.core.api.Result;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/cmt/attend")
@Tag(name = "考勤管理")
@RequiredArgsConstructor
public class CmtAttendController {

    private final ICmtAttendService attendService;

    @GetMapping("/today")
    @Operation(summary = "获取用户今日考勤信息")
    public Result<UserAttendInfoVO> getAttendToday() {
        UserAttendInfoVO result = attendService.getUserTodayAttend();
        return Result.success(result);
    }

    @GetMapping("/date/{year}/{month}/{day}")
    @Operation(summary = "获取用户当天考勤")
    public Result<UserAttendInfoVO> getAttendByDate(@PathVariable int year, @PathVariable int month, @PathVariable int day) {
        UserAttendInfoVO result = attendService.getUserAttendByDate(year, month, day);
        return Result.success(result);
    }

    @PostMapping("/reissue")
    @Operation(summary = "补卡申请")
    public Result<Void> reissueAttendApply(@RequestBody @Validated ReissueAttendDTO dto) {
        attendService.reissueAttendApplyForLoginUser(dto);
        return Result.success();
    }

    @PostMapping("/reissue-apply/callback")
    @Operation(summary = "补卡申请通过 ekp回调")
    @EkpCallbackValid
    public Result<Void> reissueAttendApplyCallBack(@RequestBody String body) {
        AttendReissueApplyPassDTO dto = JSONUtil.toBean(body, AttendReissueApplyPassDTO.class);
        attendService.doReissueAttend(dto);
        return Result.success();
    }

    @GetMapping("/used-reissue-frequency/user/{userId}/year/{year}/month/{month}")
    @Operation(summary = "查询用户当月已使用的补卡次数")
    public Result<Integer> getUsedReissueFrequency(@PathVariable("userId") String cmtUserId,
                                                   @PathVariable Integer year,
                                                   @PathVariable Integer month) {
        Integer usedFrequency = attendService.getUsedReissueFrequency(cmtUserId, year, month);
        return Result.success(usedFrequency);
    }


    @GetMapping("/abnormal/month/{month}")
    @Operation(summary = "获取月异常考勤记录")
    public Result<List<UserAttendRecordVO>> getMonthAbnormal(@PathVariable Integer month) {
        List<UserAttendRecordVO> result = attendService.getMonthAbnormal(month);
        return Result.success(result);
    }

    @PostMapping("/leave/request")
    @Operation(summary = "提交请假申请")
    public Result<Void> addLeaveRequest(@RequestBody @Validated AttendLeaveRequestDTO dto) {
        attendService.addLeaveRequest(dto);
        return Result.success();
    }


    @PostMapping("/outgoing/request")
    @Operation(summary = "提交外出申请")
    public Result<Void> addOutgoingRequest(@RequestBody @Validated AttendOutgoingRequestDTO dto) {
        attendService.addOutgoingRequest(dto);
        return Result.success();
    }

    @PostMapping("/biz-trip/request")
    @Operation(summary = "提交出差申请")
    public Result<Void> addBizTripRequest(@RequestBody @Validated AttendBizTripRequestDTO dto) {
        attendService.addBizTripRequest(dto);
        return Result.success();
    }

    @PostMapping("/overtime/request")
    @Operation(summary = "提交加班申请")
    public Result<Void> addOvertimeRequest(@RequestBody @Validated AttendOvertimeRequestDTO dto) {
        attendService.addOvertimeRequest(dto);
        return Result.success();
    }


    @PostMapping("/leave-request/revoke/{id}")
    @Operation(summary = "撤销请假申请")
    public Result<Void> revokeLeaveRequest(@PathVariable String id) {
        attendService.revokeLeaveRequest(id);
        return Result.success();
    }

    @PostMapping("/outgoing-request/revoke/{id}")
    @Operation(summary = "撤销外出申请")
    public Result<Void> revokeOutgoingRequest(@PathVariable String id) {
        attendService.revokeOutgoingRequest(id);
        return Result.success();
    }

    @PostMapping("/biz-trip-request/revoke/{id}")
    @Operation(summary = "撤销出差申请")
    public Result<Void> revokeBizTripRequest(@PathVariable String id) {
        attendService.revokeBizTripRequest(id);
        return Result.success();
    }

    @PostMapping("/overtime-request/revoke/{id}")
    @Operation(summary = "撤销加班申请")
    public Result<Void> revokeOvertimeRequest(@PathVariable String id) {
        attendService.revokeOvertimeRequest(id);
        return Result.success();
    }

    @GetMapping("/ekp/leave-duration")
    @Operation(summary = "EKP获取考勤工时")
    @EkpCallbackValid
    public Result<AttendDurationVO> getLeaveDurationForEkp(@ParameterObject AttendLeaveDurationQuery query) {
        AttendDurationVO duration = attendService.getAttendDurationByEkpUserId(query.getUserEkpId(), query.getBeginTime(), query.getEndTime());
        return Result.success(duration);
    }

    @GetMapping("/ekp/outgoing-duration")
    @Operation(summary = "EKP获取外出工时")
    @EkpCallbackValid
    public Result<AttendDurationVO> getOutgoingDurationForEkp(@ParameterObject AttendOutgoingDurationQuery query) {
        AttendDurationVO duration = attendService.getOutgoingDurationByEkpUserId(query);
        return Result.success(duration);
    }

    @GetMapping("/ekp/overtime-duration")
    @Operation(summary = "EKP获取加班工时")
    @EkpCallbackValid
    public Result<BigDecimal> getOvertimeDurationForEkp(@ParameterObject AttendOvertimeDurationQuery query) {
        BigDecimal duration = attendService.getOvertimeDurationByEkpUserId(query);
        return Result.success(duration);
    }

    @GetMapping("/ekp/biz-trip-duration")
    @Operation(summary = "EKP获取出差天数")
    @EkpCallbackValid
    public Result<AttendDurationVO> getBizTripDurationForEkp(@ParameterObject AttendBizTripDurationQuery query) {
        AttendDurationVO duration = attendService.getBizTripDurationByEkpUserId(query);
        return Result.success(duration);
    }


    @PostMapping("/ekp/outgoing-request/callback")
    @Operation(summary = "EKP外出流程审批回调")
    @EkpCallbackValid
    public Result<Void> ekpOutgoingRequestCallback(@RequestBody String body) {
        AttendOutgoingRequestEkpCallbackDTO dto = JSONUtil.toBean(body, AttendOutgoingRequestEkpCallbackDTO.class);
        attendService.saveOrUpdateOutgoingRequestStatus(dto);
        return Result.success();
    }

    @PostMapping("/ekp/leave-request/callback")
    @Operation(summary = "EKP请假流程审批回调")
    @EkpCallbackValid
    public Result<Void> ekpLeaveRequestCallback(@RequestBody String body) {
        AttendLeaveRequestEkpCallbackDTO dto = JSONUtil.toBean(body, AttendLeaveRequestEkpCallbackDTO.class);
        attendService.saveOrUpdateLeaveRequestStatus(dto);
        return Result.success();
    }

    @PostMapping("/ekp/biz-trip-request/callback")
    @Operation(summary = "EKP出差流程审批回调")
    @EkpCallbackValid
    public Result<Void> ekpBizTripRequestCallback(@RequestBody String body) {
        AttendBizTripRequestEkpCallbackDTO dto = JSONUtil.toBean(body, AttendBizTripRequestEkpCallbackDTO.class);
        attendService.saveOrUpdateBizTripRequestStatus(dto);
        return Result.success();
    }

    @PostMapping("/ekp/overtime-request/callback")
    @Operation(summary = "EKP加班流程审批回调")
    @EkpCallbackValid
    public Result<Void> ekpOvertimeRequestCallback(@RequestBody String body) {
        AttendOvertimeRequestEkpCallbackDTO dto = JSONUtil.toBean(body, AttendOvertimeRequestEkpCallbackDTO.class);
        attendService.saveOrUpdateOvertimeRequestStatus(dto);
        return Result.success();
    }

    @GetMapping("/outgoing-duration")
    @Operation(summary = "获取当前用户外出工时")
    public Result<BigDecimal> getOutgoingDuration(@ParameterObject AttendOutgoingDurationQuery query) {
        BigDecimal duration = attendService.getCurrentUserOutgoingDuration(query);
        return Result.success(duration);
    }

    @GetMapping("/leave-duration")
    @Operation(summary = "获取当前用户考勤工时")
    public Result<AttendDurationVO> getLeaveDuration(@ParameterObject AttendLeaveDurationQuery query) {
        AttendDurationVO duration = attendService.getCurrentUserLeaveDuration(query.getBeginTime(), query.getEndTime());
        return Result.success(duration);
    }

    @GetMapping("/overtime-duration")
    @Operation(summary = "获取当前用户加班工时")
    public Result<BigDecimal> getOvertimeDuration(@ParameterObject AttendOvertimeDurationQuery query) {
        BigDecimal duration = attendService.getCurrentUserOvertimeDuration(query);
        return Result.success(duration);
    }

    @GetMapping("/biz-trip-duration")
    @Operation(summary = "获取当前用户出差工时")
    public Result<AttendDurationVO> getBizTripDuration(@ParameterObject AttendBizTripDurationQuery query) {
        AttendDurationVO duration = attendService.getCurrentUserBizTripDuration(query);
        return Result.success(duration);
    }

    @GetMapping("/user/outgoing/list")
    @Operation(summary = "当前用户外出记录列表")
    public Result<List<AttendOutgoingRequestVO>> getUserOutgoingRequestList() {
        List<AttendOutgoingRequestVO> records = attendService.getUserOutgoingRequestList();
        return Result.success(records);
    }

    @GetMapping("/user/leave/list")
    @Operation(summary = "当前用户请假记录列表")
    public Result<List<AttendLeaveRequestVO>> getUserLeaveRequestList() {
        List<AttendLeaveRequestVO> records = attendService.getUserLeaveRequestList();
        return Result.success(records);
    }

    @GetMapping("/user/biz-trip/list")
    @Operation(summary = "当前用户出差记录列表")
    public Result<List<AttendBizTripRequestVO>> getUserBizTripRequestList() {
        List<AttendBizTripRequestVO> records = attendService.getUserBizTripRequestList();
        return Result.success(records);
    }

    @GetMapping("/user/overtime/list")
    @Operation(summary = "当前用户加班记录列表")
    public Result<List<AttendOvertimeRequestVO>> getUserOvertimeRequestList() {
        List<AttendOvertimeRequestVO> records = attendService.getUserOvertimeRequestList();
        return Result.success(records);
    }

    @GetMapping("/user/month/attend")
    @Operation(summary = "查询用户当月考勤数据")
    public Result<IPage<AttendMonthDataVO>> getUserMonthAttend(@ParameterObject @Validated AttendMonthDataQuery query) {
        IPage<AttendMonthDataVO> records = attendService.getUserMonthAttendData(query);
        return Result.success(records);
    }

    @GetMapping("/user/month/attend/export")
    @Operation(summary = "导出当月考勤数据")
    public Result<Void> exportUserMonthAttend(@ParameterObject @Validated AttendMonthDataQuery query) {
        attendService.exportUserMonthAttend(query);
        return Result.success();
    }

    @GetMapping("/export/list")
    @Operation(summary = "考勤数据导出记录列表")
    public Result<List<FileExportVO>> getProblemExportList() {
        List<FileExportVO> records = attendService.getAttendDataExportList();
        return Result.success(records);
    }

    @GetMapping("/leave-request/{leaveRequestId}")
    @Operation(summary = "请假申请详情")
    public Result<AttendLeaveRequestDetailVO> getLeaveRequestDetail(@PathVariable String leaveRequestId) {
        AttendLeaveRequestDetailVO detail = attendService.getLeaveRequestDetail(leaveRequestId);
        return Result.success(detail);
    }

    @GetMapping("/outgoing-request/{outgoingRequestId}")
    @Operation(summary = "外出申请详情")
    public Result<AttendOutgoingRequestDetailVO> getOutgoingRequestDetail(@PathVariable String outgoingRequestId) {
        AttendOutgoingRequestDetailVO detail = attendService.getOutgoingRequestDetail(outgoingRequestId);
        return Result.success(detail);
    }

    @GetMapping("/biz-trip-request/{bizTripRequestId}")
    @Operation(summary = "出差申请详情")
    public Result<AttendBizTripRequestDetailVO> getBizTripRequestDetail(@PathVariable String bizTripRequestId) {
        AttendBizTripRequestDetailVO detail = attendService.getBizTripRequestDetail(bizTripRequestId);
        return Result.success(detail);
    }

    @GetMapping("/overtime-request/{overtimeRequestId}")
    @Operation(summary = "加班申请详情")
    public Result<AttendOvertimeRequestDetailVO> getOvertimeRequestDetail(@PathVariable String overtimeRequestId) {
        AttendOvertimeRequestDetailVO detail = attendService.getOvertimeRequestDetail(overtimeRequestId);
        return Result.success(detail);
    }

    @GetMapping("/reissue-request")
    @Operation(summary = "补卡申请详情")
    public Result<AttendReissueDetailVO> getReissueRequestDetail(@RequestParam("ruleCheckinTime") String ruleCheckinTime) {
        AttendReissueDetailVO detail = attendService.getReissueRequestDetail(ruleCheckinTime);
        return Result.success(detail);
    }


}
