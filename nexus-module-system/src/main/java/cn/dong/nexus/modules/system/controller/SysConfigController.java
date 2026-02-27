package cn.dong.nexus.modules.system.controller;

import cn.dong.nexus.core.api.Result;
import cn.dong.nexus.modules.system.domain.dto.SysConfigDTO;
import cn.dong.nexus.modules.system.domain.vo.SysSettingVO;
import cn.dong.nexus.modules.system.service.ISysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sys/config")
@Tag(name = "配置管理")
@RequiredArgsConstructor
public class SysConfigController {

    private final ISysConfigService service;

    @GetMapping("/setting")
    @Operation(summary = "获取系统设置")
    public Result<SysSettingVO> getSetting() {
        SysSettingVO settingVO = service.getSettingConfig();
        return Result.success(settingVO);
    }

    @PostMapping
    @Operation(summary = "更新配置")
    public Result<Void> update(@RequestBody SysConfigDTO dto) {
        service.update(dto);
        return Result.success();
    }


}
