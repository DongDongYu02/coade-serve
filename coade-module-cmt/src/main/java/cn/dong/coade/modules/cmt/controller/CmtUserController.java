package cn.dong.coade.modules.cmt.controller;

import cn.dong.coade.modules.cmt.domain.dto.CmtUserPermissionDTO;
import cn.dong.coade.modules.cmt.domain.entity.CmtUser;
import cn.dong.coade.modules.cmt.domain.query.CmtUserQuery;
import cn.dong.coade.modules.cmt.domain.vo.CmtUserPermissionVO;
import cn.dong.coade.modules.cmt.domain.vo.CmtUserSelectionVO;
import cn.dong.coade.modules.cmt.domain.vo.CmtUserVO;
import cn.dong.coade.modules.cmt.service.ICmtUserService;
import cn.dong.nexus.core.api.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cmt/user")
@Tag(name = "员工管理")
@RequiredArgsConstructor
public class CmtUserController {

    public final ICmtUserService cmtUserService;

    @GetMapping
    @Operation(summary = "员工列表")
    public Result<IPage<CmtUserVO>> pageList(@ParameterObject CmtUserQuery query) {
        IPage<CmtUserVO> pageList = cmtUserService.getPageList(query);
        return Result.success(pageList);
    }

    @GetMapping("/selection")
    @Operation(summary = "员工选择列表")
    public Result<List<CmtUserSelectionVO>> selection() {
        List<CmtUserSelectionVO> selection = cmtUserService.getUserSelection();
        return Result.success(selection);
    }

    @GetMapping("/{id}/permissions")
    @Operation(summary = "获取员工权限")
    public Result<List<CmtUserPermissionVO>> userPermissions(@PathVariable String id) {
        List<CmtUserPermissionVO> permissions = cmtUserService.getUserPermissions(id);
        return Result.success(permissions);
    }

    @PostMapping("/{cmtUserId}/permissmons/grant")
    @Operation(summary = "权限授权")
    public Result<Void> userPermissionsGrant(@PathVariable String cmtUserId, @RequestBody CmtUserPermissionDTO dto) {
        dto.setCmtUserId(cmtUserId);
        cmtUserService.userPermissionsGrant(dto);
        return Result.success();
    }

    @PostMapping("/sync-from-ekp")
    @Operation(summary = "同步蓝凌职员数据")
    public Result<Void> syncFromEkp() {
        List<CmtUser> ekpUsers = cmtUserService.getUsersFromEkp();
        cmtUserService.updateUsersByEkpUsers(ekpUsers);
        return Result.success();
    }

}
