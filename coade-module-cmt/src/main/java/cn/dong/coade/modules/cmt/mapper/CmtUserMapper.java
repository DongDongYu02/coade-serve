package cn.dong.coade.modules.cmt.mapper;

import cn.dong.coade.modules.cmt.domain.bo.EkpWecomUserMappingBO;
import cn.dong.coade.modules.cmt.domain.entity.CmtUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CmtUserMapper extends BaseMapper<CmtUser> {

    @Select("SELECT fd_ekp_id AS ekp_id,fd_app_pk_id AS wecom_id FROM wxwork_oms_relation_model")
    List<EkpWecomUserMappingBO> selectEkpWecomUserMapping();

    @Select("""
            SELECT worm.fd_ekp_id AS ekp_id,
                               worm.fd_app_pk_id AS wecom_id,
                               hpi.fd_name AS username,
                               hpi.fd_mobile_no AS phone,
                               soe2.fd_name AS dept,
                               soe2.fd_id AS dept_id
                        FROM wxwork_oms_relation_model worm
                        INNER JOIN hr_staff_person_info hpi ON hpi.fd_id = worm.fd_ekp_id
                        INNER JOIN sys_org_element soe1 ON hpi.fd_id = soe1.fd_id
                        INNER JOIN sys_org_element soe2 ON soe1.fd_parentid = soe2.fd_id
            """)
    List<CmtUser> selectEkpWeComUsers();


    @Select("""
                    SELECT fd_parentid
                    FROM sys_org_element
                    WHERE fd_id = #{ekpId}
            """)
    String selectEkpOrgParentId(@Param("ekpId") String ekpId);


}
