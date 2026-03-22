package cn.dong.coade.modules.cmt.service.impl;

import cn.dong.coade.modules.cmt.domain.entity.CmtUser;
import cn.dong.coade.modules.cmt.domain.query.CmtUserQuery;
import cn.dong.coade.modules.cmt.domain.vo.CmtUserSelectionVO;
import cn.dong.coade.modules.cmt.domain.vo.CmtUserVO;
import cn.dong.coade.modules.cmt.mapper.CmtUserMapper;
import cn.dong.coade.modules.cmt.service.ICmtUserService;
import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.core.util.PageUtil;
import cn.dong.nexus.infra.util.DynamicDataSourceUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CmtUserServiceImpl extends ServiceImpl<CmtUserMapper, CmtUser> implements ICmtUserService {


    @Override
    public IPage<CmtUserVO> getPageList(CmtUserQuery query) {
        Page<CmtUser> page = this.page(query.toPage(), query.toQueryWrapper());
        return PageUtil.convertPage(page, CmtUserVO.class);
    }

    @Override
    public List<CmtUser> getUsersFromEkp() {
        return DynamicDataSourceUtil.switchTo(
                GlobalConstants.DataSource.EKP_SQLSERVER,
                () -> this.baseMapper.selectEkpWeComUsers()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUsersByEkpUsers(List<CmtUser> ekpUsers) {

        // 2. 规范化数据
        List<CmtUser> incomingUsers = normalizeUsers(ekpUsers);

        List<CmtUser> dbUsers = this.list();

        Map<String, CmtUser> incomingMap = incomingUsers.stream()
                .collect(Collectors.toMap(
                        CmtUser::getWeComId,
                        Function.identity(),
                        (_, b) -> b,
                        LinkedHashMap::new
                ));

        Map<String, CmtUser> dbMap = dbUsers.stream()
                .collect(Collectors.toMap(
                        CmtUser::getWeComId,
                        Function.identity(),
                        (a, _) -> a
                ));

        List<CmtUser> toInsert = new ArrayList<>();
        List<CmtUser> toUpdate = new ArrayList<>();
        List<String> toDeleteIds = new ArrayList<>();

        // 4. 新增 / 更新
        for (CmtUser incoming : incomingUsers) {
            CmtUser dbUser = dbMap.get(incoming.getWeComId());

            if (dbUser == null) {
                incoming.setId(null);
                toInsert.add(incoming);
                continue;
            }
            // 已存在：如果有变化则更新
            if (needUpdate(dbUser, incoming)) {
                incoming.setId(dbUser.getId());
                toUpdate.add(incoming);
            }
        }

        // 5. 删除：数据库存在，但本次 incoming 不存在
        for (CmtUser dbUser : dbUsers) {
            if (!incomingMap.containsKey(dbUser.getWeComId())) {
                toDeleteIds.add(dbUser.getId());
            }
        }

        // 6. 批量执行
        if (CollUtil.isNotEmpty(toDeleteIds)) {
            this.removeByIds(toDeleteIds);
        }

        if (CollUtil.isNotEmpty(toInsert)) {
            this.saveBatch(toInsert);
        }

        if (CollUtil.isNotEmpty(toUpdate)) {
            this.updateBatchById(toUpdate);
        }

    }

    @Override
    public List<CmtUserSelectionVO> getUserSelection() {
        List<CmtUser> users = this.list();
        if (users.isEmpty()) {
            return List.of();
        }
        return users.stream().map(item -> {
            CmtUserSelectionVO vo = new CmtUserSelectionVO();
            vo.setId(item.getId());
            vo.setName(item.getUsername());
            vo.setDept(item.getDept());
            vo.setEkpId(item.getEkpId());
            return vo;
        }).toList();
    }


    private List<CmtUser> normalizeUsers(List<CmtUser> users) {
        if (CollUtil.isEmpty(users)) {
            return new ArrayList<>();
        }

        return new ArrayList<>(
                users.stream()
                        .peek(item -> {
                            item.setId(null);
                            item.setIdentity(GlobalConstants.UserIdentity.NORMAL);
                            item.setStatus(GlobalConstants.ENABLE_STATUS.ENABLED);
                        })
                        .collect(Collectors.toMap(
                                CmtUser::getWeComId,
                                Function.identity(),
                                (_, newVal) -> newVal,
                                LinkedHashMap::new
                        ))
                        .values()
        );
    }

    private boolean needUpdate(CmtUser dbUser, CmtUser incoming) {
        return !Objects.equals(dbUser.getUsername(), incoming.getUsername())
               || !Objects.equals(dbUser.getEkpId(), incoming.getEkpId())
               || !Objects.equals(dbUser.getStatus(), incoming.getStatus())
               || !Objects.equals(dbUser.getIdentity(), incoming.getIdentity())
               || !Objects.equals(dbUser.getAvatar(), incoming.getAvatar())
               || !Objects.equals(dbUser.getPhone(), incoming.getPhone())
               || !Objects.equals(dbUser.getDept(), incoming.getDept());
    }

}
