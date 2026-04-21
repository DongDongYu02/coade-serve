package cn.dong.coade.modules.cmt.controller;

import cn.dong.coade.modules.cmt.domain.dto.*;
import cn.dong.coade.modules.cmt.domain.query.AttendLeaveDurationQuery;
import cn.dong.coade.modules.cmt.domain.query.AttendOutgoingDurationQuery;
import cn.dong.coade.modules.cmt.domain.vo.AttendLeaveRequestVO;
import cn.dong.coade.modules.cmt.domain.vo.AttendOutgoingRequestVO;
import cn.dong.coade.modules.cmt.domain.vo.UserAttendInfoVO;
import cn.dong.coade.modules.cmt.domain.vo.UserAttendRecordVO;
import cn.dong.coade.modules.cmt.service.ICmtAttendService;
import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.core.api.Result;
import cn.dong.nexus.infra.util.RedisUtil;
import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

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
    public Result<Void> reissueAttendApplyCallBack(@RequestBody String body, @RequestParam("access_token") String accessToken) {
        Object token = RedisUtil.get(GlobalConstants.CacheKey.EKP_PROVIDE_TOKEN);
        if (Objects.isNull(token)) {
            return Result.error("ekp callback accessToken has expired!");
        }
        if (!String.valueOf(token).equals(accessToken)) {
            return Result.error("ekp callback accessToken is invalid!");
        }
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

    @GetMapping("/ekp/leave-duration")
    @Operation(summary = "EKP获取考勤工时")
    public Result<BigDecimal> getLeaveDurationForEkp(@ParameterObject AttendLeaveDurationQuery query, @RequestParam("access_token") String accessToken) {
        Object token = RedisUtil.get(GlobalConstants.CacheKey.EKP_PROVIDE_TOKEN);
        if (Objects.isNull(token)) {
            return Result.error("ekp callback accessToken has expired!");
        }
        if (!String.valueOf(token).equals(accessToken)) {
            return Result.error("ekp callback accessToken is invalid!");
        }
        BigDecimal duration = attendService.getLeaveDurationByEkpUserId(query.getUserEkpId(), query.getBeginTime(), query.getEndTime());
        return Result.success(duration);
    }

    @GetMapping("/ekp/outgoing-duration")
    @Operation(summary = "EKP获取外出工时")
    public Result<BigDecimal> getOutgoingDurationForEkp(@ParameterObject AttendOutgoingDurationQuery query, @RequestParam("access_token") String accessToken) {
        Object token = RedisUtil.get(GlobalConstants.CacheKey.EKP_PROVIDE_TOKEN);
        if (Objects.isNull(token)) {
            return Result.error("ekp callback accessToken has expired!");
        }
        if (!String.valueOf(token).equals(accessToken)) {
            return Result.error("ekp callback accessToken is invalid!");
        }
        BigDecimal duration = attendService.getOutgoingDurationByEkpUserId(query);
        return Result.success(duration);
    }

    @PostMapping("/ekp/outgoing-request/callback")
    @Operation(summary = "EKP外出流程审批回调")
    public Result<Void> ekpOutgoingRequestCallback(@RequestBody String body, @RequestParam("access_token") String accessToken) {
        Object token = RedisUtil.get(GlobalConstants.CacheKey.EKP_PROVIDE_TOKEN);
        if (Objects.isNull(token)) {
            return Result.error("ekp callback accessToken has expired!");
        }
        if (!String.valueOf(token).equals(accessToken)) {
            return Result.error("ekp callback accessToken is invalid!");
        }
        AttendOutgoingRequestEkpCallbackDTO dto = JSONUtil.toBean(body, AttendOutgoingRequestEkpCallbackDTO.class);
        attendService.saveOrUpdateOutgoingRequestStatus(dto);
        return Result.success();
    }

    @PostMapping("/ekp/leave-request/callback")
    @Operation(summary = "EKP请假流程审批回调")
    public Result<Void> ekpLeaveRequestCallback(@RequestBody String body, @RequestParam("access_token") String accessToken) {
        Object token = RedisUtil.get(GlobalConstants.CacheKey.EKP_PROVIDE_TOKEN);
        if (Objects.isNull(token)) {
            return Result.error("ekp callback accessToken has expired!");
        }
        if (!String.valueOf(token).equals(accessToken)) {
            return Result.error("ekp callback accessToken is invalid!");
        }
        AttendLeaveRequestEkpCallbackDTO dto = JSONUtil.toBean(body, AttendLeaveRequestEkpCallbackDTO.class);
        attendService.saveOrUpdateLeaveRequestStatus(dto);
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
    public Result<BigDecimal> getLeaveDuration(@ParameterObject AttendLeaveDurationQuery query) {
        BigDecimal duration = attendService.getCurrentUserLeaveDuration(query.getBeginTime(), query.getEndTime());
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


}
