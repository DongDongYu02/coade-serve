package cn.dong.coade.modules.cmt.service;

import cn.dong.coade.modules.cmt.domain.entity.CmtAttendReissue;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDateTime;
import java.util.List;

public interface ICmtAttendReissueService extends IService<CmtAttendReissue> {

    List<CmtAttendReissue> getUserReissueRecordsByTimeRange(String ekpId, LocalDateTime beginTime, LocalDateTime endTime);

    List<CmtAttendReissue> getUsersReissueRecordsByTimeRange(List<String> ekpIds, LocalDateTime beginTime, LocalDateTime endTime);
}
