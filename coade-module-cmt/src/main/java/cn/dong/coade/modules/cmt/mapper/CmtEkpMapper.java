package cn.dong.coade.modules.cmt.mapper;

import cn.dong.coade.modules.cmt.domain.entity.CmtDepartment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CmtEkpMapper {
    @Select("""
            SELECT fd_id AS ekp_org_id,fd_name AS name,fd_no AS ekp_no
            FROM sys_org_element
            WHERE fd_org_type = 2
            AND fd_is_available = 1
            """)
    List<CmtDepartment> selectEkpDepartments();
}
