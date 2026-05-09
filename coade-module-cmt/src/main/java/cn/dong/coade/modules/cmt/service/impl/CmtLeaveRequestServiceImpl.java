package cn.dong.coade.modules.cmt.service.impl;

import cn.dong.coade.modules.cmt.domain.bo.AttendBusinessBO;
import cn.dong.coade.modules.cmt.domain.entity.CmtLeaveRequest;
import cn.dong.coade.modules.cmt.mapper.CmtLeaveRecordMapper;
import cn.dong.coade.modules.cmt.service.ICmtLeaveRequestService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CmtLeaveRequestServiceImpl extends ServiceImpl<CmtLeaveRecordMapper, CmtLeaveRequest>
        implements ICmtLeaveRequestService {

    @Override
    public List<AttendBusinessBO> getUserRequestByDateRange(String userId, LocalDateTime beginTime, LocalDateTime endTime) {
        List<CmtLeaveRequest> requests = this.lambdaQuery().eq(CmtLeaveRequest::getUserId, userId)
                .ge(CmtLeaveRequest::getEndTime, beginTime)
                .le(CmtLeaveRequest::getBeginTime, endTime)
                .list();
        if (requests.isEmpty()) {
            return List.of();
        }
        return requests.stream().map(item -> {
            AttendBusinessBO businessBO = new AttendBusinessBO();
            businessBO.setUserId(item.getUserId());
            businessBO.setReason(item.getReason());
            businessBO.setStartTime(item.getBeginTime());
            businessBO.setEndTime(item.getEndTime());
            businessBO.setDuration(item.getDuration());
            return businessBO;
        }).toList();
    }
}
