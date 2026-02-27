package cn.dong.nexus.modules.rbac.domain.query;

import cn.dong.nexus.core.annotations.Query;
import cn.dong.nexus.core.base.PageQuery;
import cn.dong.nexus.modules.rbac.domain.entity.SysUser;
import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * <p> 用户信息 条件查询对象 </p>
 *
 * @author Dong
 * @since 2023-11-29 16:27:20
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserQuery extends PageQuery<SysUser> {

    @Schema(description = "用户名")
    @Query(SqlKeyword.LIKE)
    private String username;

    @Schema(description = "昵称")
    @Query(SqlKeyword.LIKE)
    private String nickname;

    @Schema(description = "手机号")
    @Query(SqlKeyword.LIKE)
    private String phone;

    @Schema(hidden = true)
    @Query(SqlKeyword.DESC)
    private LocalDateTime createTime;

    @Schema(description = "创建日期-开始 yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Query(value = SqlKeyword.GE, column = "createTime")
    private LocalDateTime createTimeBegin;

    @Schema(description = "创建日期-结束 yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Query(value = SqlKeyword.LE, column = "createTime")
    private LocalDateTime createTimeEnd;


}
