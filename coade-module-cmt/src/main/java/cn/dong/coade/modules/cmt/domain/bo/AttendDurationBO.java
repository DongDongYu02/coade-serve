package cn.dong.coade.modules.cmt.domain.bo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AttendDurationBO {

    private BigDecimal duration;

    private String durationFormat;
}
