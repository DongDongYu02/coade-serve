package cn.dong.coade.modules.cmt.service.impl;

import cn.dong.coade.modules.cmt.domain.dto.PmsVehicleInfoDTO;
import cn.dong.coade.modules.cmt.domain.entity.PmsVehicleInfo;
import cn.dong.coade.modules.cmt.mapper.PmsVehicleInfoMapper;
import cn.dong.coade.modules.cmt.service.IPmsVehicleInfoService;
import cn.dong.nexus.common.constants.GlobalConstants;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@DS(GlobalConstants.DataSource.PMS_POSTGRESQL)
public class PmsVehicleInfoServiceImpl extends ServiceImpl<PmsVehicleInfoMapper, PmsVehicleInfo> implements IPmsVehicleInfoService {
    private static final String UNIQUE_NUMBER_PREFIX = "C293";

    @Override
    public void addVehicle(PmsVehicleInfoDTO dto) {
        dto.doValidate();
        PmsVehicleInfo entity = dto.toEntity();
        this.setDefaultValues(entity);
        this.save(entity);
    }

    private void setDefaultValues(PmsVehicleInfo pmsVehicleInfo) {
        pmsVehicleInfo.setPlateColor(0)
                .setPlateType(0)
                .setVehicleColor(0)
                .setVehicleType(0)
                .setParkingType(0)
                .setCardNo("")
                .setBrand("")
                .setOwnerAddress("")
                .setOwnerPhoneNumber("")
                .setIdentityNumber("")
                .setRegisterTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .setBeginTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .setEndTime(LocalDateTime.of(2099, 12, 31, 23, 59, 59))
                .setOwnerGender(null)
                .setOwnerWorkplace("")
                .setOwnerDepartment("")
                .setOwnerPost("")
                .setUniqueNumber(nextUniqueNumber(UNIQUE_NUMBER_PREFIX, this.baseMapper.selectMaxUniqueNumber(UNIQUE_NUMBER_PREFIX)))
                .setCategoryBelonged(1)
                .setExpiredHandling(null)
                .setMatchedHandling(null)
                .setTimestamp(LocalDateTime.now())
                .setReserve1(null)
                .setReserve2(null)
                .setParkPermission("1")
                .setVehicleInfoCardType(0)
                .setGroupBelonged(0)
                .setIsAlreayBag(1)
                .setIsFromThirdSystem(0)
                .setUploadFlag(0)
                .setHistoryTime("")
                .setExtraInfo(":::::")
                .setDailyTime("")
                .setRemark("")
                .setVersion(1)
                .setPeriodTypeInfo("");
    }

    private void buildUniqueNumber() {

    }

    private String nextUniqueNumber(String prefix, String currentMax) {
        if (prefix == null || prefix.isBlank()) {
            throw new IllegalArgumentException("编号前缀不能为空");
        }

        if (currentMax == null || currentMax.isBlank()) {
            return prefix + "1";
        }

        if (!currentMax.startsWith(prefix)) {
            throw new IllegalArgumentException("编号格式错误，必须以 " + prefix + " 开头：" + currentMax);
        }

        String seqStr = currentMax.substring(prefix.length());

        if (seqStr.isBlank()) {
            throw new IllegalArgumentException("编号流水号为空：" + currentMax);
        }

        int seq;
        try {
            seq = Integer.parseInt(seqStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("编号流水号不是数字：" + currentMax, e);
        }

        return prefix + (seq + 1);
    }


}
