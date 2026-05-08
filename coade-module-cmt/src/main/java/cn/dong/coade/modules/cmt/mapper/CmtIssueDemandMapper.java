package cn.dong.coade.modules.cmt.mapper;

import cn.dong.coade.modules.cmt.domain.entity.CmtIssueDemand;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

public interface CmtIssueDemandMapper extends BaseMapper<CmtIssueDemand> {
    @Select("""
            SELECT MAX(serial_no)
            FROM cmt_issue_demand
            WHERE serial_no LIKE CONCAT(#{prefix}, '%')
            """)
    String selectMaxSerialNo(String noPrefix);
}
