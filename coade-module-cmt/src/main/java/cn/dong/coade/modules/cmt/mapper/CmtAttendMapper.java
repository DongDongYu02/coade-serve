package cn.dong.coade.modules.cmt.mapper;

import cn.dong.coade.modules.cmt.domain.bo.EkpAttendBusinessBO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CmtAttendMapper {

    @Select("""
             SELECT sabt.fd_org_id,
                           sab.fd_bus_start_time AS start_time,
                           sab.fd_bus_end_time AS end_time,
                           sab.fd_process_name AS title
                    FROM sys_attend_business sab
                             LEFT JOIN sys_attend_business_target sabt ON sab.fd_id = sabt.fd_business_id
                    WHERE sabt.fd_org_id = #{ekpId}
                      AND sab.fd_del_flag = 0
                      AND sab.fd_bus_end_time >= #{beginTime}
                      AND sab.fd_bus_start_time <= #{endTime}
                      AND sab.fd_type = #{type}
            """)
    List<EkpAttendBusinessBO> selectUserEkpAttendBusiness(@Param("ekpId") String ekpId, @Param("beginTime") LocalDateTime beginTime, @Param("endTime") LocalDateTime endTime, @Param("type") Integer type);

    @Select("""
                        SELECT top 1
                        t1.fd_name
            FROM sys_attend_category_target_new sacen
            LEFT JOIN sys_org_element soe ON soe.fd_id = sacen.fd_org_id
            LEFT JOIN (SELECT fd_id,fd_name
            FROM [dbo].[sys_attend_his_category]
            WHERE fd_begin_time <= GETDATE()
              AND fd_end_time >= GETDATE()
              AND fd_is_available = 1) t1 ON sacen.his_category_id_id = t1.fd_id
            LEFT JOIN sys_attend_his_category sahc ON sacen.his_category_id_id = sahc.fd_id
            WHERE sacen.fd_org_id = #{ekpId}
            AND sacen.fd_begin_time <= GETDATE()
            ORDER BY sacen.fd_begin_time DESC
            """)
    String selectOrgAttendGroupName(@Param("ekpId") String ekpId);
}
