package cn.dong.coade.modules.cmt.controller;

import cn.dong.coade.modules.cmt.service.ICmtDepartmentService;
import cn.dong.nexus.core.api.Result;
import cn.dong.nexus.core.base.SelectionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cmt/dept")
@Tag(name = "部门管理")
@RequiredArgsConstructor
public class CmtDepartmentController {

    private final ICmtDepartmentService cmtDepartmentService;

    @GetMapping("/selection")
    @Operation(summary = "部门选择列表")
    public Result<List<SelectionVO<String, String>>> selection() {
        List<SelectionVO<String, String>> selection = cmtDepartmentService.getDepartmentSelection();
        return Result.success(selection);
    }

}
