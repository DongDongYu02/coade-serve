package cn.dong.coade.modules.cmt.support;

import cn.dong.coade.modules.cmt.mapper.CmtIssueDemandMapper;
import cn.dong.nexus.core.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class DemandNoGenerator {

    private final StringRedisTemplate stringRedisTemplate;
    private final CmtIssueDemandMapper cmtIssueDemandMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 生成需求编号
     * 示例：XQ202605060001
     */
    public String generateXqNo() {
        return generateNo("XQ");
    }
    /**
     * 生成业务编号
     *
     * @param prefix 编号前缀，例如 XQ
     */
    public String generateNo(String prefix) {
        String date = LocalDate.now().format(DATE_FORMATTER);

        String redisKey = "serial:no:" + prefix + ":" + date;

        // 1. Redis 不存在时，先用数据库最大编号初始化
        initRedisSequenceIfAbsent(redisKey, prefix, date);

        // 2. Redis 原子自增
        Long seq = stringRedisTemplate.opsForValue().increment(redisKey);

        if (seq == null) {
            throw new BizException("生成编号失败");
        }

        // 3. 返回编号
        return prefix + date + String.format("%04d", seq);
    }

    /**
     * Redis 没有计数器时，用数据库当天最大编号初始化
     */
    private void initRedisSequenceIfAbsent(String redisKey, String prefix, String date) {
        Boolean hasKey = stringRedisTemplate.hasKey(redisKey);

        if (hasKey) {
            return;
        }

        String noPrefix = prefix + date;

        // 查询数据库当天最大编号，例如 XQ202605060056
        String maxNo = cmtIssueDemandMapper.selectMaxSerialNo(noPrefix);

        long maxSeq = parseSeq(maxNo, noPrefix);

        Duration expireDuration = getExpireDuration();

        // setIfAbsent 可以防止并发下多个线程重复初始化
        stringRedisTemplate.opsForValue().setIfAbsent(
                redisKey,
                String.valueOf(maxSeq),
                expireDuration
        );
    }

    /**
     * 从编号中解析流水号
     * 例如：
     * maxNo = XQ202605060056
     * noPrefix = XQ20260506
     * 返回 56
     */
    private long parseSeq(String maxNo, String noPrefix) {
        if (maxNo == null || maxNo.isBlank()) {
            return 0L;
        }

        String seqStr = maxNo.substring(noPrefix.length());

        return Long.parseLong(seqStr);
    }

    /**
     * 设置 Redis key 到当天结束后过期
     */
    private Duration getExpireDuration() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        return Duration.between(now, endOfDay);
    }
}