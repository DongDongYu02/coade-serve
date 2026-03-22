package cn.dong.coade.modules.cmt.job;

import cn.dong.coade.modules.cmt.domain.entity.CmtUser;
import cn.dong.coade.modules.cmt.service.ICmtUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CmtUserJob {

    private final ICmtUserService cmtUserService;


    @Scheduled(cron = "0 0 0 * * *")
    public void syncUsersFromEkp() {
        List<CmtUser> ekpUsers = cmtUserService.getUsersFromEkp();
        cmtUserService.updateUsersByEkpUsers(ekpUsers);
    }
}
