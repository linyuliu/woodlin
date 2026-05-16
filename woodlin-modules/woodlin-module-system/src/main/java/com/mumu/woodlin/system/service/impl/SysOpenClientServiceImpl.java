package com.mumu.woodlin.system.service.impl;

import java.util.List;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.mumu.woodlin.common.enums.ResultCode;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.system.entity.SysOpenClient;
import com.mumu.woodlin.system.mapper.SysOpenClientMapper;
import com.mumu.woodlin.system.service.ISysOpenClientService;

/**
 * 开放平台客户服务实现。
 *
 * @author mumu
 * @since 2026-06-03
 */
@Service
@RequiredArgsConstructor
public class SysOpenClientServiceImpl extends ServiceImpl<SysOpenClientMapper, SysOpenClient>
    implements ISysOpenClientService {

    @Override
    public List<SysOpenClient> listClients(String keyword) {
        LambdaQueryWrapper<SysOpenClient> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysOpenClient::getDeleted, "0");
        wrapper.and(StrUtil.isNotBlank(keyword), nested -> nested
            .like(SysOpenClient::getClientCode, keyword)
            .or()
            .like(SysOpenClient::getClientName, keyword)
            .or()
            .like(SysOpenClient::getOwnerName, keyword));
        wrapper.orderByDesc(SysOpenClient::getUpdateTime).orderByDesc(SysOpenClient::getCreateTime);
        return list(wrapper);
    }

    @Override
    public boolean createClient(SysOpenClient client) {
        ensureClientCodeUnique(client.getClientCode(), null);
        client.setStatus(StrUtil.blankToDefault(client.getStatus(), "1"));
        return save(client);
    }

    @Override
    public boolean updateClient(SysOpenClient client) {
        if (client.getClientId() == null) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "客户ID不能为空");
        }
        ensureClientCodeUnique(client.getClientCode(), client.getClientId());
        client.setStatus(StrUtil.blankToDefault(client.getStatus(), "1"));
        return updateById(client);
    }

    private void ensureClientCodeUnique(String clientCode, Long excludeId) {
        if (StrUtil.isBlank(clientCode)) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "客户编码不能为空");
        }
        LambdaQueryWrapper<SysOpenClient> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysOpenClient::getClientCode, clientCode);
        wrapper.eq(SysOpenClient::getDeleted, "0");
        wrapper.ne(excludeId != null, SysOpenClient::getClientId, excludeId);
        if (count(wrapper) > 0) {
            throw BusinessException.of(ResultCode.CONFLICT, "客户编码已存在");
        }
    }
}
