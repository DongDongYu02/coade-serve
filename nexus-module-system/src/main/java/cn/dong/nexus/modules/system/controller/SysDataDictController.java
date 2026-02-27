package cn.dong.nexus.modules.system.controller;

import cn.dong.nexus.core.api.Result;
import cn.dong.nexus.core.resmapping.annotation.ResultTranslate;
import cn.dong.nexus.core.valid.ValidGroup;
import cn.dong.nexus.modules.system.domain.dto.SysDataDictDTO;
import cn.dong.nexus.modules.system.domain.dto.SysDataDictItemDTO;
import cn.dong.nexus.modules.system.domain.query.SysDataDictQuery;
import cn.dong.nexus.modules.system.domain.vo.SysDataDictItemVO;
import cn.dong.nexus.modules.system.domain.vo.SysDataDictVO;
import cn.dong.nexus.modules.system.domain.vo.detail.SysDataDictDetailVO;
import cn.dong.nexus.modules.system.service.ISysDataDictService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Delete;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sys/data-dict")
@Tag(name = "数据字典")
@RequiredArgsConstructor
public class SysDataDictController {

    private final ISysDataDictService service;

    @GetMapping
    @Operation(summary = "分页列表")
    @ResultTranslate
    public Result<IPage<SysDataDictVO>> pageList(@ParameterObject SysDataDictQuery query) {
        IPage<SysDataDictVO> pageList = service.getPageList(query);
        return Result.success(pageList);
    }

    @PostMapping
    @Operation(summary = "新增")
    public Result<Void> create(@RequestBody @Validated(ValidGroup.Create.class) SysDataDictDTO dto) {
        service.create(dto);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "编辑")
    public Result<Void> update(@RequestBody @Validated(ValidGroup.Update.class) SysDataDictDTO dto) {
        service.update(dto);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "详情")
    @ResultTranslate
    public Result<SysDataDictDetailVO> detail(@PathVariable String id) {
        SysDataDictDetailVO detail = service.getDetailById(id);
        return Result.success(detail);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除")
    public Result<Void> delete(@PathVariable String id) {
        service.deleteById(id);
        return Result.success();
    }

    @GetMapping("/{id}/items")
    @Operation(summary = "获取字典项")
    public Result<List<SysDataDictItemVO>> getDictItems(@PathVariable String id) {
        List<SysDataDictItemVO> dictItems = service.getDictItems(id);
        return Result.success(dictItems);
    }

    @PostMapping("/{id}/item")
    @Operation(summary = "新增字典项")
    public Result<Void> createDictItem(@PathVariable String id, @RequestBody @Validated SysDataDictItemDTO dto) {
        dto.setDataDictId(id);
        service.createDictItem(dto);
        return Result.success();
    }

    @PutMapping("/{id}/item")
    @Operation(summary = "编辑字典项")
    public Result<Void> updateDictItem(@PathVariable String id, @RequestBody @Validated(ValidGroup.Update.class) SysDataDictItemDTO dto) {
        dto.setDataDictId(id);
        service.updateDictItem(dto);
        return Result.success();
    }

    @DeleteMapping("/item/{itemId}")
    @Operation(summary = "删除字典项")
    public Result<Void> deleteDictItem(@PathVariable String itemId) {
        service.deleteItem(itemId);
        return Result.success();
    }
}
