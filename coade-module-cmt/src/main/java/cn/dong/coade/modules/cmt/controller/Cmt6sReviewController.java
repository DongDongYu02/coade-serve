package cn.dong.coade.modules.cmt.controller;

import cn.dong.coade.modules.cmt.domain.dto.Cmt6sReviewDTO;
import cn.dong.coade.modules.cmt.domain.dto.Issue6sReviewRectifyDTO;
import cn.dong.coade.modules.cmt.domain.query.Cmt6sReviewQuery;
import cn.dong.coade.modules.cmt.domain.vo.Cmt6sReviewDetailVO;
import cn.dong.coade.modules.cmt.domain.vo.Cmt6sReviewStatusCountVO;
import cn.dong.coade.modules.cmt.domain.vo.Cmt6sReviewVO;
import cn.dong.coade.modules.cmt.service.ICmt6sReviewService;
import cn.dong.nexus.core.api.Result;
import cn.dong.nexus.core.resmapping.annotation.ResultTranslate;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

}
