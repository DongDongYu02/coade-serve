package cn.dong.coade.modules.cmt.service.impl;

import cn.dong.coade.modules.cmt.domain.entity.CmtBizTripRequest;
import cn.dong.coade.modules.cmt.mapper.CmtBizTripRequestMapper;
import cn.dong.coade.modules.cmt.service.ICmtBizTripRequestService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CmtBizTripRequestServiceImpl extends ServiceImpl<CmtBizTripRequestMapper, CmtBizTripRequest> implements ICmtBizTripRequestService {

}
