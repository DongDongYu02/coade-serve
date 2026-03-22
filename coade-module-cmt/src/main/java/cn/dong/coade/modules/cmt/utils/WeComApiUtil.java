package cn.dong.coade.modules.cmt.utils;

import cn.dong.coade.modules.cmt.domain.dto.WeComUserInfoDTO;
import cn.dong.coade.modules.cmt.domain.vo.UserAttendRecordVO;
import cn.dong.nexus.core.api.ApiMessage;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
public class WeComApiUtil {
    private static final String ACCESS_TOKEN_CACHE_KEY = "wecom:accessToken";

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
        long startTime = LocalDateTimeUtil.toEpochMilli(dateBegin) / 1000;
        long endTime = LocalDateTimeUtil.toEpochMilli(dateEnd) / 1000;
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
        return checkIndData.stream()
                .filter(item -> {
                    String exceptionType = ((JSONObject) item).getStr("exception_type");
                    return StrUtil.isBlank(exceptionType);
                })
                .map(obj -> {
                    JSONObject json = (JSONObject) obj;
                    LocalDateTime time = LocalDateTimeUtil.of(json.getLong("checkin_time") * 1000);
                    UserAttendRecordVO vo = new UserAttendRecordVO();

                    vo.setCheckinTime(LocalDateTimeUtil.format(time, "yyyy-MM-dd HH:mm"));
                    vo.setLocation(json.getStr("location_title"));
                    vo.setIsReissue(StrUtil.isNotBlank(json.getStr("notes")) ? 1 : 0);
                    return vo;
                }).toList();
    }

    public static void addUserAttend(String weComId, LocalDateTime attendTime) {
        String accessToken = getAccessToken();
        long checkinTime = LocalDateTimeUtil.toEpochMilli(attendTime) / 1000;
        String localTitle = "-";
        String localDetail = "-";
        String notes = "已补卡";
        int deviceType = 3;
        String deviceDetail = "-";

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
}
