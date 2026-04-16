package cn.dong.coade.modules.cmt.mapper;

import cn.dong.coade.modules.cmt.domain.bo.EkpAttachmentBO;
import cn.dong.coade.modules.cmt.domain.entity.CmtDepartment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
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

    @Select("""
           <script>
            SELECT
                    sam.fd_id AS att_id,
                    sam.fd_key AS att_key,
                    sam.fd_file_name AS att_name,
                    saf.fd_file_path AS att_path
                FROM sys_att_main sam
                LEFT JOIN sys_att_file saf ON sam.fd_file_id = saf.fd_id
                WHERE sam.fd_key IN
                <foreach collection="keys" item="item" open="(" separator="," close=")">
                    #{item}
                </foreach>
           </script>
            """)
    List<EkpAttachmentBO> selectAttachmentsByKeys(@Param("keys") List<String> keys);
}
