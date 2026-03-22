package cn.dong.coade.modules.cmt.controller;

import cn.dong.coade.modules.cmt.domain.query.CmtUserQuery;
import cn.dong.coade.modules.cmt.domain.vo.CmtUserSelectionVO;
import cn.dong.coade.modules.cmt.domain.vo.CmtUserVO;
import cn.dong.coade.modules.cmt.service.ICmtUserService;
import cn.dong.nexus.core.api.Result;
import cn.dong.nexus.core.security.context.IAuthContext;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cmt/user")
@Tag(name = "用户管理")
@RequiredArgsConstructor
public class CmtUserController {

    public final ICmtUserService cmtUserService;

    @GetMapping
    @Operation(summary = "用户管理")
    public Result<IPage<CmtUserVO>> pageList(@ParameterObject CmtUserQuery query) {
        IPage<CmtUserVO> pageList = cmtUserService.getPageList(query);
        return Result.success(pageList);
    }

    @GetMapping("/selection")
    @Operation(summary = "用户选择列表")
    public Result<List<CmtUserSelectionVO>> selection() {
        List<CmtUserSelectionVO> selection = cmtUserService.getUserSelection();
        return Result.success(selection);
    }


}
