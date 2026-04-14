package cn.dong.coade.modules.cmt.service.impl;

import cn.dong.coade.modules.cmt.domain.entity.CmtAttendReissue;
import cn.dong.coade.modules.cmt.mapper.CmtAttendReissueMapper;
import cn.dong.coade.modules.cmt.service.ICmtAttendReissueService;
import cn.dong.nexus.common.constants.GlobalConstants;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CmtAttendReissueServiceImpl extends ServiceImpl<CmtAttendReissueMapper, CmtAttendReissue> implements ICmtAttendReissueService {

    @Override
    @DS(GlobalConstants.DataSource.LOCAL_MYSQL)
    public List<CmtAttendReissue> getUserReissueRecordsByTimeRange(String ekpId, LocalDateTime beginTime, LocalDateTime endEnd) {
        return this.lambdaQuery()
                .eq(CmtAttendReissue::getEkpUserId, ekpId)
                // 只需要处理中或通过的记录
                .ne(CmtAttendReissue::getIsApproved, GlobalConstants.AttendReissueApprovalResult.REJECTED)
                .between(CmtAttendReissue::getRuleCheckinTime, beginTime, endEnd)
                .list();
    }
}
