package cn.dong.coade.modules.cmt.service;

import cn.dong.coade.modules.cmt.domain.dto.PmsVehicleInfoDTO;
import cn.dong.coade.modules.cmt.domain.entity.PmsVehicleInfo;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IPmsVehicleInfoService extends IService<PmsVehicleInfo> {

    /**
     * 添加车牌
     */
    void addVehicle(PmsVehicleInfoDTO dto);
}
