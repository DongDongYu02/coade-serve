package cn.dong.coade.modules.cmt.controller;

import cn.dong.coade.modules.cmt.domain.dto.CleaningAreaDTO;
import cn.dong.coade.modules.cmt.domain.query.CleaningAreaQuery;
import cn.dong.coade.modules.cmt.domain.vo.CleaningAreaVO;
import cn.dong.coade.modules.cmt.service.ICmtCleaningAreaService;
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
@RequestMapping("/cmt/cleaning-area")
@Tag(name = "保洁区域管理")
@RequiredArgsConstructor
public class CmtCleaningAreaController {

    private final ICmtCleaningAreaService cleaningAreaService;

    @GetMapping
    @Operation(summary = "分页列表")
    @ResultTranslate
    public Result<IPage<CleaningAreaVO>> pageList(@ParameterObject CleaningAreaQuery query) {
        IPage<CleaningAreaVO> page = cleaningAreaService.getPageList(query);
        return Result.success(page);
    }

    @PostMapping
    @Operation(summary = "新增")
    public Result<Void> create(@RequestBody @Validated(ValidGroup.Create.class) CleaningAreaDTO dto) {
        cleaningAreaService.create(dto);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "编辑")
    public Result<Void> update(@RequestBody @Validated(ValidGroup.Update.class) CleaningAreaDTO dto) {
        cleaningAreaService.update(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除")
    public Result<Void> delete(@PathVariable String id) {
        cleaningAreaService.removeById(id);
        return Result.success();
    }

    @GetMapping("/principal/list")
    @Operation(summary = "获取当前保洁的负责区域")
    @ResultTranslate
    public Result<List<CleaningAreaVO>> getListByCurrentUser() {
        List<CleaningAreaVO> areas = cleaningAreaService.getListByCurrentUser();
        return Result.success(areas);
    }

}
