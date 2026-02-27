package cn.dong.nexus.modules.system.service;

import cn.dong.nexus.modules.system.domain.dto.SysConfigDTO;
import cn.dong.nexus.modules.system.domain.entity.SysConfig;
import cn.dong.nexus.modules.system.domain.vo.SysSettingVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

public interface ISysConfigService extends IService<SysConfig> {

    SysSettingVO getSettingConfig();

    String getValueByKey(String key);

    Map<String,String> getValuesByGroup(Integer group);


    void update(SysConfigDTO dto);
}
