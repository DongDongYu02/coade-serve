package cn.dong.coade.modules.cmt.service.impl;

import cn.dong.coade.modules.cmt.constants.CmtLocalConstants;
import cn.dong.coade.modules.cmt.domain.bo.AttendBusinessBO;
import cn.dong.coade.modules.cmt.domain.entity.CmtOvertimeRequest;
import cn.dong.coade.modules.cmt.mapper.CmtOvertimeRequestMapper;
import cn.dong.coade.modules.cmt.service.ICmtOvertimeRequestService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CmtOvertimeRequestServiceImpl
        extends ServiceImpl<CmtOvertimeRequestMapper, CmtOvertimeRequest>
        implements ICmtOvertimeRequestService {

    @Override
    public List<AttendBusinessBO> getUserRequestByDateRange(String userId, LocalDateTime beginTime, LocalDateTime endTime) {
        List<CmtOvertimeRequest> requests = this.lambdaQuery().eq(CmtOvertimeRequest::getUserId, userId)
                .ge(CmtOvertimeRequest::getOvertimeDate, beginTime)
                .le(CmtOvertimeRequest::getOvertimeDate, endTime)
                .eq(CmtOvertimeRequest::getStatus, CmtLocalConstants.ATTEND_REQUEST_STATUS.APPROVED)
                .list();
        if (requests.isEmpty()) {
            return List.of();
        }
        return buildAttendBusinessBo(requests);
    }

    @Override
    public List<AttendBusinessBO> getUsersRequestByDateRange(List<String> userIds, LocalDateTime beginTime, LocalDateTime endTime) {
        List<CmtOvertimeRequest> requests = this.lambdaQuery().in(CmtOvertimeRequest::getUserId, userIds)
                .ge(CmtOvertimeRequest::getOvertimeDate, beginTime)
                .le(CmtOvertimeRequest::getOvertimeDate, endTime)
                .eq(CmtOvertimeRequest::getStatus, CmtLocalConstants.ATTEND_REQUEST_STATUS.APPROVED)
                .list();
        if (requests.isEmpty()) {
            return List.of();
        }
        return buildAttendBusinessBo(requests);
    }

    private List<AttendBusinessBO> buildAttendBusinessBo(List<CmtOvertimeRequest> requests) {
        return requests.stream().map(item -> {
            AttendBusinessBO businessBO = new AttendBusinessBO();
            businessBO.setUserId(item.getUserId());
            businessBO.setReason(item.getReason());
            businessBO.setStartTime(item.getOvertimeDate().atTime(item.getBeginTime()));
            businessBO.setEndTime(item.getOvertimeDate().atTime(item.getEndTime()));
            businessBO.setDuration(item.getDuration());
            return businessBO;
        }).toList();
    }
}
