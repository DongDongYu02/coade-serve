package cn.dong.coade.modules.cmt.service;

import cn.dong.coade.modules.cmt.domain.dto.PmsVehicleInfoDTO;
import cn.dong.coade.modules.cmt.domain.entity.PmsVehicleInfo;
import cn.dong.coade.modules.cmt.domain.vo.VehicleInfoVO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IPmsVehicleInfoService extends IService<PmsVehicleInfo> {

    /**
     * 添加车牌
     */
    void addVehicle(PmsVehicleInfoDTO dto);

    /**
     * 根据车牌号获取车辆信息
     */
    VehicleInfoVO getVehicleInfo(String plateNo);

}
