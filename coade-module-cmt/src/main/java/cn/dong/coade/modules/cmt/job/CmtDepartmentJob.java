package cn.dong.coade.modules.cmt.job;

import cn.dong.coade.modules.cmt.service.ICmtDepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CmtDepartmentJob {

    private final ICmtDepartmentService cmtDepartmentService;

    @Scheduled(cron = "0 0 0 * * *")
    public void syncDepartmentFromEkp() {
        cmtDepartmentService.syncFromEkp();
    }
}
