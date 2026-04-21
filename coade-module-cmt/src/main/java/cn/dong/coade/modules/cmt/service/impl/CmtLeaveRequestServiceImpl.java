package cn.dong.coade.modules.cmt.service.impl;

import cn.dong.coade.modules.cmt.domain.entity.CmtLeaveRequest;
import cn.dong.coade.modules.cmt.mapper.CmtLeaveRecordMapper;
import cn.dong.coade.modules.cmt.service.ICmtLeaveRequestService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CmtLeaveRequestServiceImpl extends ServiceImpl<CmtLeaveRecordMapper, CmtLeaveRequest>
        implements ICmtLeaveRequestService {

}
