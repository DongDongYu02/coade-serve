package cn.dong.coade.modules.cmt.domain.query;

import cn.dong.coade.modules.cmt.domain.entity.CmtUser;
import cn.dong.coade.modules.cmt.service.impl.CmtUserServiceImpl;
import cn.dong.nexus.core.annotations.Query;
import cn.dong.nexus.core.base.PageQuery;
import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "CMT用户 列表查询对象")
public class CmtUserQuery extends PageQuery<CmtUser> {

    @Schema(description = "用户名")
    @Query(SqlKeyword.EQ)
    private String username;
}
