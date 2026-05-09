package cn.dong.coade.modules.cmt.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AttendRequestType {

    LEAVE(5, "请假"),
    OUT(7, "外出"),
    BIZ_TRIP(4, "出差"),
    OVERTIME(6, "加班");

    private final Integer code;
    private final String desc;
}
