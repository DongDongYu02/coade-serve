package cn.dong.nexus.common.domain.vo;

import lombok.Data;

@Data
public class FileExportVO {
    private String id;

    private String path;

    private String name;

    private Integer status;

    private String createTime;

    private String message;
}
