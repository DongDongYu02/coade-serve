package cn.dong.coade.modules.cmt.domain.bo;

import lombok.Data;

@Data
public class Cmt6SProblemResultBO {
    /**
     * 6S维度：
     * sort / setInOrder / shine / standardize / sustain / safety
     */
    private String dimension;

    /**
     * 问题标题
     */
    private String title;

    /**
     * 证据描述
     */
    private String evidence;

    /**
     * 问题发生时间点，格式：mm:ss
     */
    private String time;

    /**
     * 问题发生时间范围，格式：mm:ss-mm:ss
     */
    private String timeRange;

    /**
     * 严重程度：low / medium / high
     */
    private String severity;

    /**
     * 扣分
     */
    private Integer deduction;

    /**
     * 整改建议
     */
    private String suggestion;

    /**
     * 输入的文件类型
     */
    private String fileType;

    /**
     * 输入的文件 ID
     */
    private String sourceId;

    /**
     * 输入的文件路径
     */
    private String filePath;
}
