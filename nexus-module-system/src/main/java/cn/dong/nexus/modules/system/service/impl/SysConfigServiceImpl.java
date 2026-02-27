package cn.dong.nexus.modules.system.service.impl;

import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.core.security.context.IAuthContext;
import cn.dong.nexus.modules.system.domain.dto.SysConfigDTO;
import cn.dong.nexus.modules.system.domain.entity.SysConfig;
import cn.dong.nexus.modules.system.domain.vo.SysSettingVO;
import cn.dong.nexus.modules.system.mapper.SysConfigMapper;
import cn.dong.nexus.modules.system.service.ISysConfigService;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements ISysConfigService {

    @Override
    public SysSettingVO getSettingConfig() {
        Map<String, String> settingConfig = this.getValuesByGroup(GlobalConstants.ConfigGroup.SETTING);
        SysSettingVO vo = new SysSettingVO();
        if (settingConfig.isEmpty()) {
            return vo;
        }
        vo.setSystemName(settingConfig.getOrDefault(GlobalConstants.ConfigKey.SYS_NAME, "管理后台"));
        vo.setSystemLogo(settingConfig.getOrDefault(GlobalConstants.ConfigKey.SYS_LOGO, ""));
        return vo;
    }

    @Override
    public String getValueByKey(String key) {
        SysConfig config = this.lambdaQuery().select(SysConfig::getValue)
                .eq(SysConfig::getKey, key)
                .one();
        return Objects.isNull(config) ? null : config.getValue();
    }

    @Override
    public Map<String, String> getValuesByGroup(Integer group) {
        List<SysConfig> configs = this.lambdaQuery()
                .eq(SysConfig::getGroup, group)
                .list();
        if (configs.isEmpty()) {
            return Map.of();
        }
        return configs.stream().collect(Collectors.toMap(SysConfig::getKey, item -> Objects.isNull(item.getValue()) ? StrUtil.EMPTY : item.getValue()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysConfigDTO dto) {
        List<SysConfigDTO.Config> configs = dto.getConfigs();
        List<SysConfig> sysConfigs = BeanUtil.copyToList(configs, SysConfig.class);
        sysConfigs.forEach(item -> {
            item.setUpdateTime(LocalDateTime.now());
            item.setUpdateBy(SpringUtil.getBean(IAuthContext.class).getLoginUser().getId());
        });
        this.updateBatchById(sysConfigs);
    }
}
