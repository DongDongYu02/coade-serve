package cn.dong.coade.modules.cmt.service.impl;

import cn.dong.coade.modules.cmt.domain.bo.AttendBusinessBO;
import cn.dong.coade.modules.cmt.domain.entity.CmtOutgoingRequest;
import cn.dong.coade.modules.cmt.mapper.CmtOutgoingRequestMapper;
import cn.dong.coade.modules.cmt.service.ICmtOutgoingRequestService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CmtOutgoingRequestServiceImpl extends ServiceImpl<CmtOutgoingRequestMapper, CmtOutgoingRequest> implements ICmtOutgoingRequestService {

    @Override
    public List<AttendBusinessBO> getUserRequestByDateRange(String userId, LocalDateTime beginTime, LocalDateTime endTime) {
        LocalDate beginDate = beginTime.toLocalDate();
        LocalDate endDate = endTime.toLocalDate();
        List<CmtOutgoingRequest> requests = this.lambdaQuery().eq(CmtOutgoingRequest::getUserId, userId)
                .ge(CmtOutgoingRequest::getOutDate, beginDate)
                .le(CmtOutgoingRequest::getOutDate, endDate)
                .list();
        if (requests.isEmpty()) {
            return List.of();
        }
        return requests.stream().map(item -> {
            AttendBusinessBO businessBO = new AttendBusinessBO();
            businessBO.setUserId(item.getUserId());
            businessBO.setReason(item.getReason());
            businessBO.setStartTime(item.getOutDate().atTime(item.getOutTimeBegin()));
            businessBO.setEndTime(item.getOutDate().atTime(item.getOutTimeEnd()));
            businessBO.setDuration(item.getDuration());
            return businessBO;
        }).toList();
    }
}
