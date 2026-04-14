package cn.dong.coade.modules.cmt.controller;

import cn.dong.coade.modules.cmt.domain.dto.IssueDemandAssessmentedDTO;
import cn.dong.coade.modules.cmt.domain.dto.IssueDemandCompletedDTO;
import cn.dong.coade.modules.cmt.domain.dto.IssueDemandDTO;
import cn.dong.coade.modules.cmt.domain.dto.IssueDemandRejectDTO;
import cn.dong.coade.modules.cmt.domain.query.IssueDemandQuery;
import cn.dong.coade.modules.cmt.domain.vo.IssueDemandDetailVO;
import cn.dong.coade.modules.cmt.domain.vo.IssueDemandVO;
import cn.dong.coade.modules.cmt.service.ICmtIssueDemandService;
import cn.dong.nexus.core.api.Result;
import cn.dong.nexus.core.base.SelectionVO;
import cn.dong.nexus.core.resmapping.annotation.ResultTranslate;
import cn.dong.nexus.core.valid.ValidGroup;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cmt/issue-demand")
@Tag(name = "问题需求")
@RequiredArgsConstructor
public class CmtIssueDemandController {

    private final ICmtIssueDemandService issueDemandService;

    @PostMapping
    @Operation(summary = "新增")
    public Result<Void> create(@RequestBody @Validated(ValidGroup.Create.class) IssueDemandDTO dto) {
        issueDemandService.create(dto);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "编辑")
    public Result<Void> edit(@RequestBody @Validated(ValidGroup.Update.class) IssueDemandDTO dto) {
        issueDemandService.edit(dto);
        return Result.success();
    }


    @GetMapping("/list")
    @Operation(summary = "列表")
    @ResultTranslate
    public Result<List<IssueDemandVO>> list(@ParameterObject IssueDemandQuery query) {
        List<IssueDemandVO> page = issueDemandService.getList(query);
        return Result.success(page);
    }

    @GetMapping("/page")
    @Operation(summary = "分页列表")
    public Result<IPage<IssueDemandVO>> pageList(@ParameterObject IssueDemandQuery query) {
        IPage<IssueDemandVO> page = issueDemandService.getPageList(query);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "详情")
    public Result<IssueDemandDetailVO> detail(@PathVariable String id) {
        IssueDemandDetailVO detail = issueDemandService.getDetailById(id);
        return Result.success(detail);
    }

    @PutMapping("/{id}/assigned/{principalUserId}")
    @Operation(summary = "指派负责人")
    public Result<Void> assigned(@PathVariable String principalUserId, @PathVariable String id) {
        issueDemandService.assigned(id, principalUserId);
        return Result.success();
    }

    @PutMapping("/{id}/assessmented")
    @Operation(summary = "完成评估")
    public Result<Void> assessmented(@RequestBody IssueDemandAssessmentedDTO dto) {
        issueDemandService.assessmented(dto);
        return Result.success();
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "驳回")
    public Result<Void> reject(@RequestBody IssueDemandRejectDTO dto) {
        issueDemandService.reject(dto);
        return Result.success();
    }

    @PutMapping("/{id}/completed")
    @Operation(summary = "处理完成")
    public Result<Void> completed(@RequestBody IssueDemandCompletedDTO dto) {
        issueDemandService.completed(dto);
        return Result.success();
    }

    @GetMapping("/principal/selection")
    @Operation(summary = "负责人列表")
    public Result<List<SelectionVO<String,String>>> getPrincipalSelection(){
        List<SelectionVO<String,String>> result = issueDemandService.getPrincipalSelection();
        return Result.success(result);
    }


}
