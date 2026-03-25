package cn.dong.coade.modules.cmt.utils;

import cn.dong.coade.modules.cmt.domain.bo.EkpAttendRuleBO;
import cn.dong.coade.modules.cmt.domain.dto.WeComUserInfoDTO;
import cn.dong.coade.modules.cmt.domain.vo.UserAttendRecordVO;
import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.core.api.ApiMessage;
import cn.dong.nexus.core.config.properties.CoadeProperties;
import cn.dong.nexus.core.exception.BizException;
import cn.dong.nexus.infra.util.RedisUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
public class WeComApiUtil {
    private static final String ACCESS_TOKEN_CACHE_KEY = "wecom:accessToken";
    private static final String ATTEND_RULE_CACHE_KEY_PREFIX = "wecom:attend_rule";
    private static final String REISSUE_NOTES = "已补卡";
    private static final String CORP_ID = SpringUtil.getProperty("coade.cmt.we-com-corp-id");

    private static final String SECRET = SpringUtil.getProperty("coade.cmt.we-com-secret");

    private static String getAccessToken() {
        Object accessTokenCache = RedisUtil.get(ACCESS_TOKEN_CACHE_KEY);
        if (Objects.nonNull(accessTokenCache)) {
            return (String) accessTokenCache;
        }
        String url = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=" + CORP_ID + "&corpsecret=" + SECRET;
        String resp = HttpUtil.get(url);
        JSONObject respJson = JSONUtil.parseObj(resp);
        if (respJson.getInt("errcode") != 0) {
            log.error("获取企微AccessToken失败：{}", respJson.getStr("errmsg"));
            throw new BizException("企微免登授权失败，请稍后重试！");
        }
        String accessToken = respJson.getStr("access_token");
        RedisUtil.set(ACCESS_TOKEN_CACHE_KEY, accessToken, 7200, TimeUnit.SECONDS);
        return accessToken;
    }

    public static WeComUserInfoDTO getUserInfo(String code) {
        String accessToken = getAccessToken();
        String userTicket = getUserTicket(accessToken, code);
        String url = StrUtil.format("https://qyapi.weixin.qq.com/cgi-bin/auth/getuserdetail?access_token={}", accessToken);
        String resp = HttpUtil.post(url, JSONUtil.toJsonStr(Map.of("user_ticket", userTicket)));
        JSONObject respJson = JSONUtil.parseObj(resp);
        if (respJson.getInt("errcode") != 0) {
            log.error("获取企微UserInfo失败：{}", respJson.getStr("errmsg"));
            throw new BizException("企微免登授权失败，请稍后重试！");
        }
        WeComUserInfoDTO dto = new WeComUserInfoDTO();
        dto.setUserId(respJson.getStr("userid"));
        dto.setMobile(respJson.getStr("mobile"));
        dto.setGender(respJson.getStr("gender"));
        dto.setEmail(respJson.getStr("email"));
        dto.setAvatar(respJson.getStr("avatar"));
        dto.setQrCode(respJson.getStr("qr_code"));
        dto.setBizEmail(respJson.getStr("biz_email"));
        dto.setAddress(respJson.getStr("address"));
        return dto;
    }


    public static String getUsername(String userId) {
        String accessToken = getAccessToken();
        String url = StrUtil.format("https://qyapi.weixin.qq.com/cgi-bin/user/get?access_token={}&userid={}", accessToken, userId);
        String resp = HttpUtil.get(url);
        JSONObject respJson = JSONUtil.parseObj(resp);
        if (respJson.getInt("errcode") != 0) {
            log.error("获取企微Username失败：{}", respJson.getStr("errmsg"));
            throw new BizException("企微免登授权失败，请稍后重试！");
        }
        return respJson.getStr("name");
    }

    public static String getUserTicket(String accessToken, String code) {
        String url = StrUtil.format("https://qyapi.weixin.qq.com/cgi-bin/auth/getuserinfo?access_token={}&code={}", accessToken, code);
        String resp = HttpUtil.post(url, Map.of());
        JSONObject respJson = JSONUtil.parseObj(resp);
        if (respJson.getInt("errcode") != 0) {
            log.error("获取企微UserTicket失败：{}", respJson.getStr("errmsg"));
            throw new BizException("企微免登授权失败，请稍后重试！");
        }
        return respJson.getStr("user_ticket");
    }

    public static List<UserAttendRecordVO> getUserAttend(String weComId, LocalDateTime dateBegin, LocalDateTime dateEnd) {
        // 打卡记录
        List<UserAttendRecordVO> checkinRecords = new ArrayList<>(getCheckinRecords(weComId, dateBegin, dateEnd));
        // 补卡后的打卡记录
        List<UserAttendRecordVO> reissueRecords = getReissueRecords(weComId, dateBegin, dateEnd);
        checkinRecords.addAll(reissueRecords);
        return CollUtil.distinct(checkinRecords);
    }

    public static void addUserAttend(String weComId, LocalDateTime attendTime) {
        String accessToken = getAccessToken();
        long checkinTime = LocalDateTimeUtil.toEpochMilli(attendTime) / 1000;
        String localTitle = "-";
        String localDetail = "-";
        int deviceType = 3;
        String deviceDetail = "-";

        JSONObject attendParams = new JSONObject();
        attendParams.set("userid", weComId)
                .set("checkin_time", checkinTime)
                .set("location_title", localTitle)
                .set("location_detail", localDetail)
                .set("notes", REISSUE_NOTES)
                .set("device_type", deviceType)
                .set("device_detail", deviceDetail);
        JSONObject body = new JSONObject().set("records", List.of(attendParams));
        String url = StrUtil.format("https://qyapi.weixin.qq.com/cgi-bin/checkin/add_checkin_record?access_token={}", accessToken);
        String resp = HttpUtil.post(url, JSONUtil.toJsonStr(body));
        JSONObject respJson = JSONUtil.parseObj(resp);
        if (respJson.getInt("errcode") != 0) {
            log.error("添加企微补卡记录失败：{}", respJson.getStr("errmsg"));
            throw new BizException(ApiMessage.INTERNAL_ERROR);
        }
        log.info("用户:{} 补卡成功，补卡时间:{}", weComId, attendTime);

    }

    public static EkpAttendRuleBO getUserAttendRule(String weComId, LocalDateTime attendDate) {
        String cacheKey = StrUtil.format(ATTEND_RULE_CACHE_KEY_PREFIX + ":{}:{}", weComId, LocalDateTimeUtil.toEpochMilli(attendDate));
        EkpAttendRuleBO cache = RedisUtil.get(cacheKey, EkpAttendRuleBO.class);
        if (Objects.nonNull(cache)) {
            return cache;
        }
        String accessToken = getAccessToken();
        long datetime = LocalDateTimeUtil.toEpochMilli(attendDate) / 1000;
        String url = StrUtil.format("https://qyapi.weixin.qq.com/cgi-bin/checkin/getcheckinoption?access_token={}", accessToken);
        JSONObject body = new JSONObject();
        body.set("useridlist", new String[]{weComId}).set("datetime", datetime);
        try {
            String resp = HttpUtil.post(url, JSONUtil.toJsonStr(body));
            JSONObject respJson = JSONUtil.parseObj(resp);
            if (respJson.getInt("errcode") != 0) {
                log.error("获取职员考勤规则失败：{}", respJson.getStr("errmsg"));
                return null;
            }
            if (respJson.getJSONArray("info").isEmpty()) {
                return null;
            }
            JSONObject ruleInfo = respJson.getJSONArray("info")
                    .getJSONObject(0)
                    .getJSONObject("group")
                    .getJSONArray("checkindate").getJSONObject(0);
            JSONArray workdays = ruleInfo.getJSONArray("workdays");
            JSONArray checkintime = ruleInfo.getJSONArray("checkintime");

            int[] workDays = workdays.stream().mapToInt(item -> (int) item).toArray();
            String[][] timeRanges = checkintime.stream().map(item -> {
                JSONObject time = (JSONObject) item;
                Integer workSec = time.getInt("work_sec");
                Integer offWorkSec = time.getInt("off_work_sec");

                int hours = workSec / 3600;
                int minutes = (workSec % 3600) / 60;
                String timePoint1 = String.format("%02d:%02d", hours, minutes);
                hours = offWorkSec / 3600;
                minutes = (offWorkSec % 3600) / 60;
                String timePoint2 = String.format("%02d:%02d", hours, minutes);
                return new String[]{timePoint1, timePoint2};
            }).toArray(String[][]::new);
            EkpAttendRuleBO bo = new EkpAttendRuleBO(timeRanges, workDays);
            RedisUtil.set(cacheKey, bo, 1, TimeUnit.DAYS);
            return bo;
        } catch (Exception e) {
            log.error("获取职员考勤规则失败：{}", e.getMessage());
            throw new BizException(ApiMessage.INTERNAL_ERROR);
        }

    }


    public static void test(String weComId, LocalDateTime attendTime) {
        String accessToken = getAccessToken();
        long checkinTime = LocalDateTimeUtil.toEpochMilli(attendTime) / 1000;
        String localTitle = "9楼打卡";
        String localDetail = "";
        String notes = "";
        int deviceType = 1;
        String deviceDetail = "";

        JSONObject attendParams = new JSONObject();
        attendParams.set("userid", weComId)
                .set("checkin_time", checkinTime)
                .set("location_title", localTitle)
                .set("location_detail", localDetail)
                .set("notes", notes)
                .set("device_type", deviceType)
                .set("device_detail", deviceDetail);
        JSONObject body = new JSONObject().set("records", List.of(attendParams));
        String url = StrUtil.format("https://qyapi.weixin.qq.com/cgi-bin/checkin/add_checkin_record?access_token={}", accessToken);
        String resp = HttpUtil.post(url, JSONUtil.toJsonStr(body));
        JSONObject respJson = JSONUtil.parseObj(resp);
        if (respJson.getInt("errcode") != 0) {
            log.error("添加企微补卡记录失败：{}", respJson.getStr("errmsg"));
            throw new BizException(ApiMessage.INTERNAL_ERROR);
        }
        log.info("用户:{} 补卡成功，补卡时间:{}", weComId, attendTime);

    }

    /**
     * 获取员工打卡记录
     */
    public static List<UserAttendRecordVO> getCheckinRecords(String weComId, LocalDateTime timeBegin, LocalDateTime timeEnd) {
        List<String> deviceSns = SpringUtil.getBean(CoadeProperties.class).getAttendDeviceSn();
//        if (CollUtil.isEmpty(deviceSns)) {
//            return List.of();
//        }
        String accessToken = getAccessToken();
        long startTime = LocalDateTimeUtil.toEpochMilli(timeBegin) / 1000;
        long endTime = LocalDateTimeUtil.toEpochMilli(timeEnd) / 1000;
        String url = StrUtil.format("https://qyapi.weixin.qq.com/cgi-bin/hardware/get_hardware_checkin_data?access_token={}", accessToken);
        JSONObject body = new JSONObject();
        body.set("filter_type", 2)
                .set("starttime", startTime)
                .set("endtime", endTime)
                .set("useridlist", weComId);
        String resp = HttpUtil.post(url, JSONUtil.toJsonStr(body));
        JSONObject respJson = JSONUtil.parseObj(resp);

        return respJson.getJSONArray("checkindata").stream().filter(item -> {
            JSONObject checkintime = (JSONObject) item;
            String deviceSn = checkintime.getStr("device_sn");
            return deviceSns.contains(deviceSn);
        }).map(item -> {
            JSONObject data = (JSONObject) item;
            String checkinTime = LocalDateTimeUtil.format(LocalDateTimeUtil.of(data.getLong("checkin_time") * 1000), GlobalConstants.DatePattern.Y_M_D_H_M);
            String location = data.getStr("device_name");
            UserAttendRecordVO v = new UserAttendRecordVO();
            v.setCheckinTime(checkinTime);
            v.setLocation(location);
            v.setIsReissue(GlobalConstants.INT_NO);
            return v;
        }).toList();
    }


    /**
     * 获取员工补卡记录
     * <p>目前的补卡逻辑是新增一条该打卡点的打卡记录，notes为“补卡”</p>
     */
    public static List<UserAttendRecordVO> getReissueRecords(String weComId, LocalDateTime timeBegin, LocalDateTime timeEnd) {
        long startTime = LocalDateTimeUtil.toEpochMilli(timeBegin) / 1000;
        long endTime = LocalDateTimeUtil.toEpochMilli(timeEnd) / 1000;
        String accessToken = getAccessToken();
        String url = StrUtil.format("https://qyapi.weixin.qq.com/cgi-bin/checkin/getcheckindata?access_token={}", accessToken);
        String resp = HttpUtil.post(url, JSONUtil.toJsonStr(Map.of(
                "opencheckindatatype", 3,
                "starttime", startTime,
                "endtime", endTime,
                "useridlist", List.of(weComId))));
        JSONObject respJson = JSONUtil.parseObj(resp);
        if (respJson.getInt("errcode") != 0) {
            log.error("获取企微打卡数据失败：{}", respJson.getStr("errmsg"));
            throw new BizException(ApiMessage.INTERNAL_ERROR);
        }
        JSONArray checkIndData = respJson.getJSONArray("checkindata");
        if (CollUtil.isEmpty(checkIndData)) {
            return List.of();
        }
        // 补卡的记录
        return checkIndData.stream()
                .filter(item -> {
                    String exceptionType = ((JSONObject) item).getStr("exception_type");
                    return !"未打卡".equals(exceptionType);
                })
                .map(obj -> {
                    JSONObject json = (JSONObject) obj;
                    LocalDateTime time = LocalDateTimeUtil.of(json.getLong("checkin_time") * 1000);
                    UserAttendRecordVO vo = new UserAttendRecordVO();
                    vo.setCheckinTime(LocalDateTimeUtil.format(time, "yyyy-MM-dd HH:mm"));
                    vo.setLocation(json.getStr("location_title"));
                    vo.setIsReissue("已补卡".equals(json.getStr("notes")) ? GlobalConstants.INT_YES : GlobalConstants.INT_NO);
                    return vo;
                }).toList();
    }


}
