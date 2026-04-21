package cn.dong.coade.modules.cmt.service.impl;

import cn.dong.coade.modules.cmt.domain.entity.CmtOutgoingRequest;
import cn.dong.coade.modules.cmt.mapper.CmtOutgoingRequestMapper;
import cn.dong.coade.modules.cmt.service.ICmtOutgoingRequestService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CmtOutgoingRequestServiceImpl extends ServiceImpl<CmtOutgoingRequestMapper, CmtOutgoingRequest> implements ICmtOutgoingRequestService {

}
