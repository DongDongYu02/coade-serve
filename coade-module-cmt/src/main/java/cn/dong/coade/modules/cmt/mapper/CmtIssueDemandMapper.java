package cn.dong.coade.modules.cmt.mapper;

import cn.dong.coade.modules.cmt.domain.entity.CmtIssueDemand;
import cn.dong.coade.modules.cmt.domain.vo.IssueDemandProposeDeptTopVO;
import cn.dong.coade.modules.cmt.domain.vo.IssueDemandProposerTopVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

public interface CmtIssueDemandMapper extends BaseMapper<CmtIssueDemand> {
    @Select("""
            SELECT MAX(serial_no)
            FROM cmt_issue_demand
            WHERE serial_no LIKE CONCAT(#{prefix}, '%')
            """)
    String selectMaxSerialNo(String noPrefix);


    @Select("""
            SELECT *
            FROM (
                SELECT
                    propose_user_id,
                    propose_user_name,
                    propose_dept,
                    total,
                    RANK() OVER (ORDER BY total DESC) AS ranking
                FROM (
                    SELECT
                        propose_user_id,
                        propose_user_name,
                        propose_dept,
                        COUNT(1) AS total
                    FROM cmt_issue_demand
                    WHERE create_time >= #{beginTime}
                      AND create_time < #{endTime}
                    GROUP BY
                        propose_user_id,
                        propose_user_name,
                        propose_dept
                ) t
            ) r
            WHERE ranking <= 3
            ORDER BY ranking;
            """)
    List<IssueDemandProposerTopVO> getProposerTop(@Param("beginTime") LocalDateTime beginTime,
                                                  @Param("endTime") LocalDateTime endTime);
    @Select("""
            SELECT *
            FROM (
                SELECT
                    propose_dept,
                    total,
                    valid_total,
                    invalid_total,
                    effective_rate,
                    RANK() OVER (ORDER BY total DESC) AS ranking
                FROM (
                    SELECT
                        propose_dept,
                        COUNT(1) AS total,
                        SUM(CASE WHEN status = 5 THEN 1 ELSE 0 END) AS invalid_total,
                        SUM(CASE WHEN status = 5 THEN 0 ELSE 1 END) AS valid_total,
                        CAST(
                            ROUND(
                                SUM(CASE WHEN status = 5 THEN 0 ELSE 1 END) * 1.0 / COUNT(1),
                                4
                            ) AS DECIMAL(10, 4)
                        ) AS effective_rate
            
                    FROM cmt_issue_demand
                    WHERE create_time >= #{beginTime}
                      AND create_time < #{endTime}
                      AND propose_dept IS NOT NULL
                      AND propose_dept <> ''
                    GROUP BY propose_dept
                ) t
            ) r
            WHERE ranking <= 3
            ORDER BY ranking;
            """)
    List<IssueDemandProposeDeptTopVO> getProposeDeptTop(@Param("beginTime") LocalDateTime beginTime,
                                                        @Param("endTime") LocalDateTime endTime);
}
