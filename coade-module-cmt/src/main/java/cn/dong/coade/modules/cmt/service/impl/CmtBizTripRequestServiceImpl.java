package cn.dong.coade.modules.cmt.service.impl;

import cn.dong.coade.modules.cmt.constants.CmtLocalConstants;
import cn.dong.coade.modules.cmt.domain.bo.AttendBusinessBO;
import cn.dong.coade.modules.cmt.domain.entity.CmtBizTripRequest;
import cn.dong.coade.modules.cmt.mapper.CmtBizTripRequestMapper;
import cn.dong.coade.modules.cmt.service.ICmtBizTripRequestService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CmtBizTripRequestServiceImpl extends ServiceImpl<CmtBizTripRequestMapper, CmtBizTripRequest> implements ICmtBizTripRequestService {

    @Override
    public List<AttendBusinessBO> getUserRequestByDateRange(String userId, LocalDateTime beginTime, LocalDateTime endTime) {
        List<CmtBizTripRequest> requests = this.lambdaQuery().eq(CmtBizTripRequest::getUserId, userId)
                .ge(CmtBizTripRequest::getEndTime, beginTime)
                .le(CmtBizTripRequest::getBeginTime, endTime)
                .eq(CmtBizTripRequest::getStatus, CmtLocalConstants.ATTEND_REQUEST_STATUS.APPROVED)
                .list();
        if (requests.isEmpty()) {
            return List.of();
        }
        return buildAttendBusinessBo(requests);
    }

    @Override
    public List<AttendBusinessBO> getUsersRequestByDateRange(List<String> userIds, LocalDateTime beginTime, LocalDateTime endTime) {
        List<CmtBizTripRequest> requests = this.lambdaQuery().in(CmtBizTripRequest::getUserId, userIds)
                .ge(CmtBizTripRequest::getEndTime, beginTime)
                .le(CmtBizTripRequest::getBeginTime, endTime)
                .eq(CmtBizTripRequest::getStatus, CmtLocalConstants.ATTEND_REQUEST_STATUS.APPROVED)
                .list();
        if (requests.isEmpty()) {
            return List.of();
        }
        return buildAttendBusinessBo(requests);
    }


    private List<AttendBusinessBO> buildAttendBusinessBo(List<CmtBizTripRequest> requests) {
        return requests.stream().map(item -> {
            AttendBusinessBO businessBO = new AttendBusinessBO();
            businessBO.setUserId(item.getUserId());
            businessBO.setReason(item.getReason());
            businessBO.setStartTime(item.getBeginTime().atStartOfDay());
            businessBO.setEndTime(item.getEndTime().atTime(23, 59, 59));
            businessBO.setDuration(item.getDuration());
            return businessBO;
        }).toList();
    }
}
