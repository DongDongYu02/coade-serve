package cn.dong.coade.modules.cmt.domain.bo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AttendBusinessBO {

    private String userId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String reason;

    private BigDecimal duration;
}
