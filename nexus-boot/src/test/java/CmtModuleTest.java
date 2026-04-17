import cn.dong.coade.modules.cmt.domain.vo.Cmt6sReviewProblemQuery;
import cn.dong.coade.modules.cmt.service.ICmt6sReviewService;
import cn.dong.nexus.NexusApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = NexusApplication.class)
public class CmtModuleTest {
    @Autowired
    private ICmt6sReviewService cmt6sReviewService;

    @Test
    public void test() {
        cmt6sReviewService.getProblemRectifyExcelData(new Cmt6sReviewProblemQuery());
    }


}
