package cn.dong.coade.modules.cmt.controller;

import cn.dong.coade.modules.cmt.domain.dto.AttendReissueApplyPassDTO;
import cn.dong.coade.modules.cmt.domain.dto.ReissueAttendDTO;
import cn.dong.coade.modules.cmt.domain.vo.UserAttendInfoVO;
import cn.dong.coade.modules.cmt.service.ICmtAttendService;
import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.core.api.Result;
import cn.dong.nexus.core.exception.BizException;
import cn.dong.nexus.core.security.context.IAuthContext;
import cn.dong.nexus.infra.util.RedisUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

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
}
