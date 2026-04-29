package cn.dong.coade.modules.cmt.domain.bo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EkpAttendBusinessBO {

    private String ekpId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String title;
}
