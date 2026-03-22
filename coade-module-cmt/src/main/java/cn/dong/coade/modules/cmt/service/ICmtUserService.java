package cn.dong.coade.modules.cmt.service;

import cn.dong.coade.modules.cmt.domain.entity.CmtUser;
import cn.dong.coade.modules.cmt.domain.query.CmtUserQuery;
import cn.dong.coade.modules.cmt.domain.vo.CmtUserSelectionVO;
import cn.dong.coade.modules.cmt.domain.vo.CmtUserVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ICmtUserService extends IService<CmtUser> {
    IPage<CmtUserVO> getPageList(CmtUserQuery query);

    List<CmtUser> getUsersFromEkp();

    void updateUsersByEkpUsers(List<CmtUser> ekpUsers);

    List<CmtUserSelectionVO> getUserSelection();
}
