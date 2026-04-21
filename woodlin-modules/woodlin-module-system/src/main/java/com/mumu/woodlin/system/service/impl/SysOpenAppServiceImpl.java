package com.mumu.woodlin.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mumu.woodlin.common.enums.ResultCode;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.system.entity.SysOpenApp;
import com.mumu.woodlin.system.entity.SysOpenAppCredential;
import com.mumu.woodlin.system.mapper.SysOpenAppCredentialMapper;
import com.mumu.woodlin.system.mapper.SysOpenAppMapper;
import com.mumu.woodlin.system.service.ISysOpenAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 开放应用服务实现。
 *
 * @author mumu
 * @since 2026-04-13
 */
@Service
@RequiredArgsConstructor
public class SysOpenAppServiceImpl extends ServiceImpl<SysOpenAppMapper, SysOpenApp> implements ISysOpenAppService {

    private final SysOpenAppCredentialMapper credentialMapper;

    @Override
    public List<SysOpenApp> listApps(String keyword) {
        LambdaQueryWrapper<SysOpenApp> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysOpenApp::getDeleted, "0");
        wrapper.and(StrUtil.isNotBlank(keyword), nested -> nested
            .like(SysOpenApp::getAppCode, keyword)
            .or()
            .like(SysOpenApp::getAppName, keyword)
            .or()
            .like(SysOpenApp::getOwnerName, keyword)
        );
        wrapper.orderByDesc(SysOpenApp::getUpdateTime).orderByDesc(SysOpenApp::getCreateTime);
        return list(wrapper);
    }

    @Override
    public boolean createApp(SysOpenApp app) {
        ensureAppCodeUnique(app.getAppCode(), null);
        app.setStatus(StrUtil.blankToDefault(app.getStatus(), "1"));
        return save(app);
    }

    @Override
    public boolean updateApp(SysOpenApp app) {
        if (app.getAppId() == null) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "应用ID不能为空");
        }
        requireExistingApp(app.getAppId());
        ensureAppCodeUnique(app.getAppCode(), app.getAppId());
        app.setStatus(StrUtil.blankToDefault(app.getStatus(), "1"));
        return updateById(app);
    }

    @Override
    public boolean removeAppCascade(Long appId) {
        requireExistingApp(appId);
        LambdaQueryWrapper<SysOpenAppCredential> credentialWrapper = new LambdaQueryWrapper<>();
        credentialWrapper.eq(SysOpenAppCredential::getAppId, appId);
        credentialMapper.delete(credentialWrapper);
        return removeById(appId);
    }

    //
    private void ensureAppCodeUnique(String appCode, Long excludeId) {
        if (StrUtil.isBlank(appCode)) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "应用编码不能为空");
        }
        LambdaQueryWrapper<SysOpenApp> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysOpenApp::getAppCode, appCode);
        wrapper.eq(SysOpenApp::getDeleted, "0");
        wrapper.ne(excludeId != null, SysOpenApp::getAppId, excludeId);
        if (count(wrapper) > 0) {
            throw BusinessException.of(ResultCode.CONFLICT, "应用编码已存在");
        }
    }

    private void requireExistingApp(Long appId) {
        SysOpenApp existing = getById(appId);
        if (existing == null || "1".equals(existing.getDeleted())) {
            throw BusinessException.of(ResultCode.NOT_FOUND, "开放应用不存在");
        }
    }
}
