package cn.dong.nexus.modules.rbac.service.impl;

import cn.dong.nexus.core.api.ApiMessage;
import cn.dong.nexus.core.exception.BizException;
import cn.dong.nexus.core.resmapping.ResMappingUtil;
import cn.dong.nexus.core.security.context.IAuthContext;
import cn.dong.nexus.core.security.enums.SysUserIdentity;
import cn.dong.nexus.core.security.utils.AesDecryptUtil;
import cn.dong.nexus.core.security.utils.PasswordUtil;
import cn.dong.nexus.core.util.PageUtil;
import cn.dong.nexus.modules.rbac.domain.dto.SysUserDTO;
import cn.dong.nexus.modules.rbac.domain.entity.SysRole;
import cn.dong.nexus.modules.rbac.domain.entity.SysUser;
import cn.dong.nexus.modules.rbac.domain.entity.SysUserRole;
import cn.dong.nexus.modules.rbac.domain.query.SysUserQuery;
import cn.dong.nexus.modules.rbac.domain.vo.SysUserVO;
import cn.dong.nexus.modules.rbac.domain.vo.UserRoleDTO;
import cn.dong.nexus.modules.rbac.domain.vo.detail.SysUserDetailVO;
import cn.dong.nexus.modules.rbac.mapper.SysUserMapper;
import cn.dong.nexus.modules.rbac.service.ISysUserRoleService;
import cn.dong.nexus.modules.rbac.service.ISysUserService;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {
    private final ISysUserRoleService sysUserRoleService;
    private final IAuthContext authContext;

    @Override
    public IPage<SysUserVO> getPageList(SysUserQuery query) {
        QueryWrapper<SysUser> queryWrapper = query.toQueryWrapper();
        queryWrapper.ne("identity", SysUserIdentity.SUPER_ADMIN.getCode()); // 排除超级管理员
        Page<SysUser> page = page(query.toPage(), queryWrapper);
        IPage<SysUserVO> pageVO = PageUtil.convertPage(page, SysUserVO.class);
        if (!pageVO.getRecords().isEmpty()) {
            // 封装额外信息
            loadExtraInfo(pageVO.getRecords());
        }
        return pageVO;
    }

    private void loadExtraInfo(List<SysUserVO> records) {
        List<String> userIds = records.stream().map(SysUserVO::getId).toList();
        // 加载用户的角色信息
        List<SysUserRole> userRoles = sysUserRoleService.lambdaQuery().in(SysUserRole::getUserId, userIds).list();
        if (userRoles.isEmpty()) {
            return;
        }
        List<String> roleIds = userRoles.stream().map(SysUserRole::getRoleId).toList();
        Map<String, String> roleMapping = ResMappingUtil.getFieldMapping(roleIds, SysRole::getId, SysRole::getName);
        Map<String, List<String[]>> userRoleInfoMap = userRoles.stream()
                .collect(Collectors.groupingBy(SysUserRole::getUserId,
                        Collectors.mapping(item -> new String[]{item.getRoleId(), roleMapping.get(item.getRoleId())}, Collectors.toList())));
        records.forEach(item -> {
            List<String[]> roleInfo = userRoleInfoMap.get(item.getId());
            if (CollUtil.isNotEmpty(roleInfo)) {
                List<String> roleIdsList = roleInfo.stream().map(arr -> arr[0]).collect(Collectors.toList());
                List<String> roleNamesList = roleInfo.stream().map(arr -> arr[1]).collect(Collectors.toList());
                item.setRoleIds(roleIdsList);
                item.setRoleNames(roleNamesList);
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(SysUserDTO dto) {
        dto.doValidate();
        // 前端密码解密
        String encrypt = AesDecryptUtil.decrypt(dto.getPassword());
        // 密码hash+salt
        String hash = PasswordUtil.encode(encrypt);
        dto.setPassword(hash);
        SysUser entity = dto.toEntity();
        this.save(entity);
        // 分配用户角色
        sysUserRoleService.grantRoles(entity.getId(), dto.getRoleIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysUserDTO dto) {
        dto.doValidate();
        this.updateById(dto.toEntity());
        // 更新用户角色
        sysUserRoleService.grantRoles(dto.getId(), dto.getRoleIds());
        // 用户强制下线
        authContext.kickout(dto.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(String id) {
        this.removeById(id);
        sysUserRoleService.lambdaUpdate().eq(SysUserRole::getUserId, id).remove();
        authContext.kickout(id);
    }

    @Override
    public SysUserDetailVO getDetailById(String id) {
        SysUser user = this.getById(id);
        if (Objects.isNull(user)) {
            throw new BizException(ApiMessage.NOT_FOUND);
        }
        SysUserDetailVO detail = BeanUtil.copyProperties(user, SysUserDetailVO.class);
        // 获取用户的角色
        UserRoleDTO userRoles = sysUserRoleService.getUserRoles(id);
        if (Objects.nonNull(userRoles)) {
            detail.setRoleIds(userRoles.getRoleIds());
            detail.setRoleNames(userRoles.getRoleNames());
        }
        return detail;
    }

    @Override
    public String resetPassword(String id) {
        SysUser user = this.getById(id);
        if (Objects.isNull(user)) {
            throw new BizException(ApiMessage.NOT_FOUND);
        }
        String randomPwd = RandomUtil.randomString(6);
        String hash = PasswordUtil.encode(randomPwd);
        this.lambdaUpdate().set(SysUser::getPassword, hash).eq(SysUser::getId, id).update();
        return randomPwd;
    }

}
