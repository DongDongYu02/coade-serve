package cn.dong.coade.modules.cmt.service.impl;

import cn.dong.coade.modules.cmt.domain.entity.CmtDepartment;
import cn.dong.coade.modules.cmt.mapper.CmtDepartmentMapper;
import cn.dong.coade.modules.cmt.service.CmtEkpService;
import cn.dong.coade.modules.cmt.service.ICmtDepartmentService;
import cn.dong.nexus.core.base.SelectionVO;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.impl.PairConverter;
import cn.hutool.core.lang.Pair;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CmtDepartmentServiceImpl extends ServiceImpl<CmtDepartmentMapper, CmtDepartment> implements ICmtDepartmentService {

    private final CmtEkpService cmtEkpService;

    @Override
    @DSTransactional(rollbackFor = Exception.class)
    public void syncFromEkp() {
        List<CmtDepartment> ekpDepartments = cmtEkpService.getEkpDepartments();
        LocalDateTime now = LocalDateTime.now();
        // 2. 规范化数据
        List<CmtDepartment> incomingUsers = normalizeDepartments(ekpDepartments);

        List<CmtDepartment> dbDepartments = this.list();

        Map<String, CmtDepartment> incomingMap = incomingUsers.stream()
                .collect(Collectors.toMap(
                        CmtDepartment::getEkpOrgId,
                        Function.identity(),
                        (_, b) -> b,
                        LinkedHashMap::new
                ));

        Map<String, CmtDepartment> dbMap = dbDepartments.stream()
                .collect(Collectors.toMap(
                        CmtDepartment::getEkpOrgId,
                        Function.identity(),
                        (a, _) -> a
                ));

        List<CmtDepartment> toInsert = new ArrayList<>();
        List<CmtDepartment> toUpdate = new ArrayList<>();
        List<String> toDeleteIds = new ArrayList<>();

        // 4. 新增 / 更新
        for (CmtDepartment incoming : incomingUsers) {
            CmtDepartment dbDepartment = dbMap.get(incoming.getEkpOrgId());

            if (dbDepartment == null) {
                incoming.setId(null);
                incoming.setUpdateTime(now);
                toInsert.add(incoming);
                continue;
            }
            // 已存在：如果有变化则更新
            if (needUpdate(dbDepartment, incoming)) {
                incoming.setId(dbDepartment.getId());
                incoming.setUpdateTime(now);
                toUpdate.add(incoming);
            }
        }

        // 5. 删除：数据库存在，但本次 incoming 不存在
        for (CmtDepartment dbDepartment : dbDepartments) {
            if (!incomingMap.containsKey(dbDepartment.getEkpOrgId())) {
                toDeleteIds.add(dbDepartment.getId());
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
    public List<SelectionVO<String, String>> getDepartmentSelection() {
        List<CmtDepartment> departments = this.lambdaQuery().select(CmtDepartment::getId, CmtDepartment::getName)
                .list();
        if (departments.isEmpty()) {
            return List.of();
        }
        return departments.stream()
                .filter(item -> !item.getName().contains("—") && !item.getName().contains("-"))
                .map(item -> new SelectionVO<>(item.getId(), item.getName())).toList();
    }

    private List<CmtDepartment> normalizeDepartments(List<CmtDepartment> departments) {
        if (CollUtil.isEmpty(departments)) {
            return new ArrayList<>();
        }

        return new ArrayList<>(
                departments.stream()
                        .peek(item -> {
                            item.setId(null);
                        })
                        .collect(Collectors.toMap(
                                CmtDepartment::getEkpOrgId,
                                Function.identity(),
                                (_, newVal) -> newVal,
                                LinkedHashMap::new
                        ))
                        .values()
        );
    }

    private boolean needUpdate(CmtDepartment dbDepartment, CmtDepartment incoming) {
        return !Objects.equals(dbDepartment.getName(), incoming.getName())
               || !Objects.equals(dbDepartment.getEkpNo(), incoming.getEkpNo());
    }


}
