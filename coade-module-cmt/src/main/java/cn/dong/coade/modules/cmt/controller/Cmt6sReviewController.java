package cn.dong.coade.modules.cmt.controller;

import cn.dong.coade.modules.cmt.domain.dto.Cmt6sReviewDTO;
import cn.dong.coade.modules.cmt.domain.dto.Issue6sReviewRectifyDTO;
import cn.dong.coade.modules.cmt.domain.query.Cmt6sReviewProblemQuery;
import cn.dong.coade.modules.cmt.domain.query.Cmt6sReviewQuery;
import cn.dong.coade.modules.cmt.domain.vo.Cmt6sReviewDetailVO;
import cn.dong.coade.modules.cmt.domain.vo.Cmt6sReviewProblemVO;
import cn.dong.coade.modules.cmt.domain.vo.Cmt6sReviewStatusCountVO;
import cn.dong.coade.modules.cmt.domain.vo.Cmt6sReviewVO;
import cn.dong.coade.modules.cmt.service.ICmt6sReviewService;
import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.common.domain.vo.FileExportVO;
import cn.dong.nexus.core.api.Result;
import cn.dong.nexus.core.resmapping.annotation.ResultTranslate;
import cn.dong.nexus.infra.util.RedisUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/cmt/6s")
@Tag(name = "6S评审")
@RequiredArgsConstructor

public class Cmt6sReviewController {

    private final ICmt6sReviewService cmt6sReviewService;

    @GetMapping
    @Operation(summary = "分页列表")
    @ResultTranslate
    public Result<IPage<Cmt6sReviewVO>> pageList(@ParameterObject Cmt6sReviewQuery query) {
        IPage<Cmt6sReviewVO> records = cmt6sReviewService.getPageList(query);
        return Result.success(records);
    }

    @PostMapping
    @Operation(summary = "新增")
    public Result<Void> create(@RequestBody @Validated Cmt6sReviewDTO dto) {
        cmt6sReviewService.create(dto);
        return Result.success();
    }

    @GetMapping("/re-analyze")
    @Operation(summary = "重新分析")
    public Result<Void> reAnalyze(@RequestParam("reviewId") String reviewId) {
        cmt6sReviewService.reAnalyze(reviewId);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "详情")
    public Result<Cmt6sReviewDetailVO> detail(@PathVariable String id) {
        Cmt6sReviewDetailVO detail = cmt6sReviewService.getDetailById(id);
        return Result.success(detail);
    }

    @GetMapping("/status-count")
    @Operation(summary = "状态统计")
    public Result<Cmt6sReviewStatusCountVO> statusCount() {
        Cmt6sReviewStatusCountVO vo = cmt6sReviewService.getStatusCount();
        return Result.success(vo);
    }

    @PostMapping("/issue-rectify")
    @Operation(summary = "发起整改")
    public Result<Void> issueRectify(@RequestBody @Validated Issue6sReviewRectifyDTO dto) {
        cmt6sReviewService.issueRectify(dto);
        return Result.success();
    }


    @PostMapping("/rectify-completed/callback")
    @Operation(summary = "整改完成")
    public Result<Void> rectifyCompleted(@RequestBody String body, @RequestParam("access_token") String accessToken) {
        Object token = RedisUtil.get(GlobalConstants.CacheKey.EKP_PROVIDE_TOKEN);
        if (Objects.isNull(token)) {
            return Result.error("ekp callback accessToken has expired!");
        }
        if (!String.valueOf(token).equals(accessToken)) {
            return Result.error("ekp callback accessToken is invalid!");
        }
        JSONObject jsonBody = JSONUtil.parseObj(body);

        cmt6sReviewService.rectifyCompleted(jsonBody);
        return Result.success();
    }

    @GetMapping("/export/problems")
    @Operation(summary = "导出整改项")
    public Result<Void> exportProblems(@ParameterObject Cmt6sReviewProblemQuery query) {
        cmt6sReviewService.exportProblemRectifyToExcel(query);
        return Result.success();
    }

    @GetMapping("/problem/page")
    @Operation(summary = "分页列表")
    @ResultTranslate
    public Result<IPage<Cmt6sReviewProblemVO>> problemPageList(@ParameterObject Cmt6sReviewProblemQuery query) {
        IPage<Cmt6sReviewProblemVO> records = cmt6sReviewService.getProblemPageList(query);
        return Result.success(records);
    }

    @GetMapping("/problem/export/list")
    @Operation(summary = "问题导出记录列表")
    public Result<List<FileExportVO>> getProblemExportList() {
        List<FileExportVO> records = cmt6sReviewService.getProblemExportList();
        return Result.success(records);
    }
}
