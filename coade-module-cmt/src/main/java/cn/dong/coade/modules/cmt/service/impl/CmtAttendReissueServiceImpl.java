package cn.dong.coade.modules.cmt.service.impl;

import cn.dong.coade.modules.cmt.domain.entity.CmtAttendReissue;
import cn.dong.coade.modules.cmt.domain.entity.CmtUser;
import cn.dong.coade.modules.cmt.mapper.CmtAttendReissueMapper;
import cn.dong.coade.modules.cmt.mapper.CmtUserMapper;
import cn.dong.coade.modules.cmt.service.ICmtAttendReissueService;
import cn.dong.coade.modules.cmt.service.ICmtAttendService;
import cn.dong.coade.modules.cmt.service.ICmtUserService;
import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.infra.util.DynamicDataSourceUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CmtAttendReissueServiceImpl extends ServiceImpl<CmtAttendReissueMapper, CmtAttendReissue> implements ICmtAttendReissueService {

}
