package cn.dong.coade.modules.cmt.service;

import cn.dong.coade.modules.cmt.domain.bo.AttendBusinessBO;
import cn.dong.coade.modules.cmt.domain.entity.CmtOvertimeRequest;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDateTime;
import java.util.List;

public interface ICmtOvertimeRequestService extends IService<CmtOvertimeRequest> {
    /**
     * 查询用户时间段内的申请记录
     */
    List<AttendBusinessBO> getUserRequestByDateRange(String userId, LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 查询多用户时间段内的申请记录
     */
    List<AttendBusinessBO> getUsersRequestByDateRange(List<String> userIds, LocalDateTime beginTime, LocalDateTime endTime);
}
