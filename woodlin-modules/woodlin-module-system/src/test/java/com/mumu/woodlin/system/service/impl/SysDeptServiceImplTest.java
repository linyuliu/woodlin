package com.mumu.woodlin.system.service.impl;

import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.system.mapper.SysDeptMapper;
import com.mumu.woodlin.system.mapper.SysUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * SysDeptServiceImpl 关键行为测试
 *
 * @author mumu
 * @since 2026-03-15
 */
class SysDeptServiceImplTest {

    private SysDeptServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SysDeptMapper deptMapper = Mockito.mock(SysDeptMapper.class);
        SysUserMapper userMapper = Mockito.mock(SysUserMapper.class);
        service = Mockito.spy(new SysDeptServiceImpl(deptMapper, userMapper));
    }

    @Test
    void deleteDeptById_shouldThrow_whenDeptHasChildren() {
        doReturn(true).when(service).hasChildByDeptId(100L);

        assertThrows(BusinessException.class, () -> service.deleteDeptById(100L));
        verify(service, never()).removeById(100L);
    }

    @Test
    void deleteDeptById_shouldThrow_whenDeptHasUsers() {
        doReturn(false).when(service).hasChildByDeptId(100L);
        doReturn(true).when(service).checkDeptExistUser(100L);

        assertThrows(BusinessException.class, () -> service.deleteDeptById(100L));
        verify(service, never()).removeById(100L);
    }

    @Test
    void deleteDeptById_shouldRemove_whenDeptCanDelete() {
        doReturn(false).when(service).hasChildByDeptId(100L);
        doReturn(false).when(service).checkDeptExistUser(100L);
        doReturn(true).when(service).removeById(100L);

        boolean result = service.deleteDeptById(100L);

        assertTrue(result);
        verify(service).removeById(100L);
    }
}

