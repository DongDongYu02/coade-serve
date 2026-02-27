package cn.dong.nexus.modules.system.service.impl;

import cn.dong.nexus.modules.system.domain.entity.SysDataDictItem;
import cn.dong.nexus.modules.system.mapper.SysDataDictItemMapper;
import cn.dong.nexus.modules.system.service.ISysDataDictItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SysDataDictItemServiceImpl extends ServiceImpl<SysDataDictItemMapper, SysDataDictItem> implements ISysDataDictItemService {

}
