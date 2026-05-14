package cn.dong.coade.modules.cmt.controller;

import cn.dong.coade.modules.cmt.domain.query.IssueDemandAnalysisQuery;
import cn.dong.coade.modules.cmt.domain.vo.*;
import cn.dong.coade.modules.cmt.service.IssueDemandAnalysisService;
import cn.dong.nexus.core.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cmt/issue-demand/analysis")
@Tag(name = "问题/优化反馈统计分析")
@RequiredArgsConstructor
public class CmtIssueDemandAnalysisController {
    private final IssueDemandAnalysisService issueDemandAnalysisService;

    @GetMapping("/overview")
    @Operation(summary = "统计总览")
    public Result<IssueDemandOverviewVO> overview(@ParameterObject @Validated IssueDemandAnalysisQuery query) {
        IssueDemandOverviewVO data = issueDemandAnalysisService.getOverview(query);
        return Result.success(data);
    }


    @GetMapping("/submit-trend")
    @Operation(summary = "上报趋势")
    public Result<List<IssueDemandSubmitTrendVO>> submitTrend(@ParameterObject @Validated IssueDemandAnalysisQuery query) {
        List<IssueDemandSubmitTrendVO> data = issueDemandAnalysisService.getSubmitTrend(query);
        return Result.success(data);
    }

    @GetMapping("/process-overview")
    @Operation(summary = "处理节点概览")
    public Result<IssueDemandProcessOverviewVO> processOverview(@ParameterObject @Validated IssueDemandAnalysisQuery query) {
        IssueDemandProcessOverviewVO data = issueDemandAnalysisService.getProcessOverview(query);
        return Result.success(data);
    }

    @GetMapping("/system-distribution")
    @Operation(summary = "系统分布")
    public Result<List<IssueDemandSystemDistributionVO>> systemDistribution(@ParameterObject @Validated IssueDemandAnalysisQuery query) {
        List<IssueDemandSystemDistributionVO> data = issueDemandAnalysisService.getSystemDistribution(query);
        return Result.success(data);
    }

    @GetMapping("/proposer-top")
    @Operation(summary = "职员提出数TOP")
    public Result<List<IssueDemandProposerTopVO>> proposerTop(@ParameterObject @Validated IssueDemandAnalysisQuery query) {
        List<IssueDemandProposerTopVO> data = issueDemandAnalysisService.getProposerTop(query);
        return Result.success(data);
    }

    @GetMapping("/propose-dept-top")
    @Operation(summary = "部门提出数TOP")
    public Result<List<IssueDemandProposeDeptTopVO>> proposeDeptTop(@ParameterObject @Validated IssueDemandAnalysisQuery query) {
        List<IssueDemandProposeDeptTopVO> data = issueDemandAnalysisService.getProposeDeptTop(query);
        return Result.success(data);
    }

    @GetMapping("/principal-statistics")
    @Operation(summary = "负责人任务统计")
    public Result<List<IssueDemandPrincipalStatisticsVO>> principalStatistics(@ParameterObject @Validated IssueDemandAnalysisQuery query) {
        List<IssueDemandPrincipalStatisticsVO> data = issueDemandAnalysisService.getPrincipalStatistics(query);
        return Result.success(data);
    }

    @GetMapping("/efficiency")
    @Operation(summary = "效率分析")
    public Result<IssueDemandOverdueEfficiencyAnalysisVO> efficiency(@ParameterObject @Validated IssueDemandAnalysisQuery query) {
        IssueDemandOverdueEfficiencyAnalysisVO data = issueDemandAnalysisService.getEfficiencyAnalysis(query);
        return Result.success(data);
    }

}
