package cn.dong.coade.modules.cmt.service.impl;

import cn.dong.coade.modules.cmt.domain.entity.CmtOvertimeRequest;
import cn.dong.coade.modules.cmt.mapper.CmtOvertimeRequestMapper;
import cn.dong.coade.modules.cmt.service.ICmtOvertimeRequestService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CmtOvertimeRequestServiceImpl
        extends ServiceImpl<CmtOvertimeRequestMapper, CmtOvertimeRequest>
        implements ICmtOvertimeRequestService {

}
