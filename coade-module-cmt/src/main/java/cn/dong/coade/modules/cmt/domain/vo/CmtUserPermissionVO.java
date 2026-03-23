package cn.dong.coade.modules.cmt.domain.vo;

import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.core.resmapping.annotation.ResMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Schema(description = "CMT用户权限 VO")
@NoArgsConstructor
public class CmtUserPermissionVO {

    @Schema(description = "ID")
    @ResMapping(sourceTable = GlobalConstants.TableName.CMT_PERMISSION,targets = "name",values = "name")
    private String id;

    @Schema(description = "权限名称")
    private String name;



    public CmtUserPermissionVO(String id) {
        this.id = id;
    }
}
