package cn.dong.coade.modules.cmt.controller;

import cn.dong.coade.modules.cmt.service.ICmtPermissionService;
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
@RequestMapping("/cmt/permission")
@Tag(name = "CMT权限管理")
@RequiredArgsConstructor
public class CmtPermissionController {
    private final ICmtPermissionService service;


    @GetMapping("/selection")
    @Operation(summary = "权限选择列表")
    public Result<List<SelectionVO<String,String>>> selection() {
        List<SelectionVO<String,String>> selection = service.getPermissionSelection();
        return Result.success(selection);
    }
}
