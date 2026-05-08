package cn.dong.coade.modules.cmt.mapper;

import cn.dong.coade.modules.cmt.domain.entity.PmsVehicleInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PmsVehicleInfoMapper extends BaseMapper<PmsVehicleInfo> {

    @Select("""
            SELECT uniquenumber
            FROM vehicleinfo
            WHERE uniquenumber LIKE CONCAT(#{prefix}, '%')
            ORDER BY CAST(SUBSTRING(uniquenumber FROM LENGTH(#{prefix}) + 1) AS INTEGER) DESC
            LIMIT 1
            """)
    String selectMaxUniqueNumber(@Param("prefix") String prefix);
}
