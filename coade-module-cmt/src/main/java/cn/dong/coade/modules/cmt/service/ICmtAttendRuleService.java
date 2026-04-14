package cn.dong.coade.modules.cmt.service;

import cn.dong.coade.modules.cmt.domain.bo.AttendRuleBO;
import cn.dong.coade.modules.cmt.domain.entity.CmtAttendRule;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDate;
import java.util.Map;

public interface ICmtAttendRuleService extends IService<CmtAttendRule> {

    /**
     * 保存所有职员当日考勤规则
     */
    void saveAllUserAttendRulesByDate(LocalDate date);

    /**
     * 获取用户当日考勤规则的
     */
    AttendRuleBO getUserAttendRule(String weComId, LocalDate date);

    /**
     * 获取用户当月每天的考勤规则
     *
     * @param month 月份
     */
    Map<Integer, AttendRuleBO> getUserAttendRuleByMonth(String weComId, Integer year, Integer month);
}
