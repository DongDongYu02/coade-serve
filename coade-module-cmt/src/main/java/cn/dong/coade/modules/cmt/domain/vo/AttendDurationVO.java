package cn.dong.coade.modules.cmt.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Schema(description = "考勤工时 VO")
@NoArgsConstructor
@AllArgsConstructor
public class AttendDurationVO {

    private BigDecimal duration;

    private String durationFormat;
}
