package cn.dong.coade.modules.cmt.service;

import cn.dong.coade.modules.cmt.domain.bo.Cmt6SProblemResultBO;
import cn.dong.coade.modules.cmt.domain.entity.Cmt6sReviewProblem;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ICmt6sReviewProblemService extends IService<Cmt6sReviewProblem> {

    void extractProblemFrameAndSave(String recordId, List<Cmt6SProblemResultBO> problem);
}
