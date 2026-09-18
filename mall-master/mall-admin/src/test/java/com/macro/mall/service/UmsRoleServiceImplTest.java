package com.macro.mall.service;

import com.macro.mall.dao.UmsRoleDao;
import com.macro.mall.mapper.UmsRoleMapper;
import com.macro.mall.mapper.UmsRoleMenuRelationMapper;
import com.macro.mall.mapper.UmsRoleResourceRelationMapper;
import com.macro.mall.service.impl.UmsRoleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UmsRoleServiceImplTest {

    @Mock
    private UmsRoleMapper roleMapper;
    @Mock
    private UmsRoleMenuRelationMapper roleMenuRelationMapper;
    @Mock
    private UmsRoleResourceRelationMapper roleResourceRelationMapper;
    @Mock
    private UmsRoleDao roleDao;
    @Mock
    private UmsAdminCacheService adminCacheService;

    private UmsRoleServiceImpl roleService;

    @BeforeEach
    void setUp() {
        roleService = new UmsRoleServiceImpl();
        ReflectionTestUtils.setField(roleService, "roleMapper", roleMapper);
        ReflectionTestUtils.setField(roleService, "roleMenuRelationMapper", roleMenuRelationMapper);
        ReflectionTestUtils.setField(roleService, "roleResourceRelationMapper", roleResourceRelationMapper);
        ReflectionTestUtils.setField(roleService, "roleDao", roleDao);
        ReflectionTestUtils.setField(roleService, "adminCacheService", adminCacheService);
    }

    @Test
    void allocMenuDeletesOldRelationsAndInsertsNewRelations() {
        int count = roleService.allocMenu(1L, List.of(10L, 20L, 30L));

        assertThat(count).isEqualTo(3);
        verify(roleMenuRelationMapper).deleteByExample(any());
        verify(roleMenuRelationMapper, times(3)).insert(any());
    }

    @Test
    void allocResourceDeletesOldRelationsInsertsNewRelationsAndClearsRoleCache() {
        int count = roleService.allocResource(1L, List.of(100L, 200L));

        assertThat(count).isEqualTo(2);
        verify(roleResourceRelationMapper).deleteByExample(any());
        verify(roleResourceRelationMapper, times(2)).insert(any());
        verify(adminCacheService).delResourceListByRole(1L);
    }

    @Test
    void deleteClearsResourceCacheForDeletedRoleIds() {
        roleService.delete(List.of(1L, 2L));

        verify(roleMapper).deleteByExample(any());
        verify(adminCacheService).delResourceListByRoleIds(List.of(1L, 2L));
    }
}
