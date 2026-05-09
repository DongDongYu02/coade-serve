package cn.dong.coade.modules.cmt.controller;

import cn.dong.coade.modules.cmt.domain.dto.PmsVehicleInfoDTO;
import cn.dong.coade.modules.cmt.domain.vo.VehicleInfoVO;
import cn.dong.coade.modules.cmt.service.IPmsVehicleInfoService;
import cn.dong.coade.modules.cmt.support.aspect.annotation.EkpCallbackValid;
import cn.dong.nexus.core.api.Result;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pms/vehicle")
@Tag(name = "PMS车辆管理")
@RequiredArgsConstructor
public class PmsVehicleInfoController {
    private final IPmsVehicleInfoService pmsVehicleInfoService;

    @PostMapping("/add")
    @Operation(summary = "添加车辆")
    @EkpCallbackValid
    public Result<Void> addVehicle(@RequestBody String body) {
        PmsVehicleInfoDTO dto = JSONUtil.toBean(body, PmsVehicleInfoDTO.class);
        if (StrUtil.hasBlank(dto.getOwnerName(), dto.getPlateNo())) {
            return Result.error("参数缺失！");
        }
        pmsVehicleInfoService.addVehicle(dto);
        return Result.success();
    }

    @GetMapping("/{plateNo}")
    @Operation(summary = "查询车辆")
    public Result<VehicleInfoVO> getVehicleInfo(@PathVariable String plateNo) {
        VehicleInfoVO vo = pmsVehicleInfoService.getVehicleInfo(plateNo);
        return Result.success(vo);
    }
}
