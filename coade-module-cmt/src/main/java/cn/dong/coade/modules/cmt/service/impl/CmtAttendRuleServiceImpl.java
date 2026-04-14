package cn.dong.coade.modules.cmt.service.impl;

import cn.dong.coade.modules.cmt.domain.bo.AttendRuleBO;
import cn.dong.coade.modules.cmt.domain.entity.CmtAttendRule;
import cn.dong.coade.modules.cmt.domain.entity.CmtUser;
import cn.dong.coade.modules.cmt.domain.enums.AttendRuleType;
import cn.dong.coade.modules.cmt.mapper.CmtAttendRuleMapper;
import cn.dong.coade.modules.cmt.service.ICmtAttendRuleService;
import cn.dong.coade.modules.cmt.service.ICmtUserService;
import cn.dong.coade.modules.cmt.utils.WeComApiUtil;
import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.infra.util.RedisUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CmtAttendRuleServiceImpl extends ServiceImpl<CmtAttendRuleMapper, CmtAttendRule> implements ICmtAttendRuleService {
    private final ICmtUserService cmtUserService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAllUserAttendRulesByDate(LocalDate date) {
        List<CmtUser> users = cmtUserService.lambdaQuery()
                .select(CmtUser::getWeComId)
                .list();
        if (users.isEmpty()) {
            return;
        }
        if (this.lambdaQuery().eq(CmtAttendRule::getAttendDate, date).exists()) {
            this.lambdaUpdate().eq(CmtAttendRule::getAttendDate, date).remove();
        }
        List<String> weComIds = users.stream().map(CmtUser::getWeComId).toList();
        List<AttendRuleBO> rules = WeComApiUtil.getAllUserAttendRules(weComIds, date);
        if (rules.isEmpty()) {
            return;
        }
        List<CmtAttendRule> attendRules = rules.stream().map(item -> {
            CmtAttendRule rule = new CmtAttendRule();
            rule.setAttendDate(date);
            rule.setWeComId(item.getWeComId());
            rule.setRule(JSONUtil.toJsonStr(item));
            return rule;
        }).toList();
        this.saveBatch(attendRules);
    }

    @Override
    @DS(GlobalConstants.DataSource.LOCAL_MYSQL)
    public AttendRuleBO getUserAttendRule(String weComId, LocalDate date) {
        String cacheKey = StrUtil.format(GlobalConstants.CacheKey.USER_ATTEND_RULE + "{}:{}", weComId, date.format(GlobalConstants.DateFormat.NORMAL_ONLY_DATE));
        // 先查缓存
        AttendRuleBO ruleBO = RedisUtil.get(cacheKey, AttendRuleBO.class);
        if (Objects.nonNull(ruleBO)) {
            return ruleBO;
        }
        // 缓存没有 查数据库
        CmtAttendRule rule = this.lambdaQuery().eq(CmtAttendRule::getWeComId, weComId)
                .eq(CmtAttendRule::getAttendDate, date)
                .one();
        if (Objects.nonNull(rule)) {
            ruleBO = JSONUtil.toBean(rule.getRule(), AttendRuleBO.class);
            RedisUtil.set(cacheKey, ruleBO);
            return ruleBO;
        }
        // 数据库也没有 查企微
        ruleBO = WeComApiUtil.getUserAttendRule(weComId, date.atStartOfDay());
        if (Objects.isNull(ruleBO)) {
            ruleBO = new AttendRuleBO(new String[][]{}, new int[]{}, AttendRuleType.EMPTY, weComId);
        }
        CmtAttendRule cmtAttendRule = new CmtAttendRule();
        cmtAttendRule.setAttendDate(date);
        cmtAttendRule.setWeComId(weComId);
        cmtAttendRule.setRule(JSONUtil.toJsonStr(ruleBO));
        this.save(cmtAttendRule);
        RedisUtil.set(cacheKey, ruleBO);
        return ruleBO;


    }

    @Override
    public Map<Integer, AttendRuleBO> getUserAttendRuleByMonth(String weComId, Integer year, Integer month) {
        LocalDate begin = LocalDate.of(year, month, 1);
        LocalDate end = begin.plusMonths(1);
        List<CmtAttendRule> rules = this.lambdaQuery()
                .ge(CmtAttendRule::getAttendDate, begin)
                .lt(CmtAttendRule::getAttendDate, end)
                .eq(CmtAttendRule::getWeComId,weComId)
                .list();
        if (rules.isEmpty()) {
            return Map.of();
        }
        return rules.stream().collect(Collectors.toMap(
                item -> item.getAttendDate().getDayOfMonth(),
                item -> JSONUtil.toBean(item.getRule(), AttendRuleBO.class)));
    }
}
