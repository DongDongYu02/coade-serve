package cn.dong.coade.modules.cmt.controller;

import cn.dong.coade.modules.cmt.domain.dto.CleaningRecordDTO;
import cn.dong.coade.modules.cmt.domain.query.CleaningRecordQuery;
import cn.dong.coade.modules.cmt.domain.vo.CleaningRecordVO;
import cn.dong.coade.modules.cmt.service.ICmtCleaningRecordService;
import cn.dong.nexus.core.api.Result;
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
@RequestMapping("/cmt/cleaning-records")
@Tag(name = "打扫记录")
@RequiredArgsConstructor
public class CmtCleaningRecordController {

    private final ICmtCleaningRecordService cleaningRecordService;

    @GetMapping
    @Operation(summary = "分页列表")
    @ResultTranslate
    public Result<IPage<CleaningRecordVO>> pageList(@ParameterObject CleaningRecordQuery query) {
        IPage<CleaningRecordVO> page = cleaningRecordService.getPageList(query);
        return Result.success(page);
    }

    @GetMapping("/list")
    @Operation(summary = "列表")
    @ResultTranslate
    public Result<List<CleaningRecordVO>> list(@ParameterObject CleaningRecordQuery query) {
        List<CleaningRecordVO> page = cleaningRecordService.getList(query);
        return Result.success(page);
    }

    @PostMapping
    @Operation(summary = "新增")
    public Result<Void> create(@RequestBody @Validated(ValidGroup.Create.class) CleaningRecordDTO dto) {
        cleaningRecordService.create(dto);
        return Result.success();
    }
}
