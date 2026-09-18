package com.macro.mall.service;

import com.macro.mall.common.service.RedisService;
import com.macro.mall.dao.UmsAdminRoleRelationDao;
import com.macro.mall.mapper.UmsAdminRoleRelationMapper;
import com.macro.mall.model.UmsAdmin;
import com.macro.mall.model.UmsAdminRoleRelation;
import com.macro.mall.service.impl.UmsAdminCacheServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UmsAdminCacheServiceImplTest {

    @Mock
    private UmsAdminService adminService;
    @Mock
    private RedisService redisService;
    @Mock
    private UmsAdminRoleRelationMapper adminRoleRelationMapper;
    @Mock
    private UmsAdminRoleRelationDao adminRoleRelationDao;

    private UmsAdminCacheServiceImpl cacheService;

    @BeforeEach
    void setUp() {
        cacheService = new UmsAdminCacheServiceImpl();
        ReflectionTestUtils.setField(cacheService, "adminService", adminService);
        ReflectionTestUtils.setField(cacheService, "redisService", redisService);
        ReflectionTestUtils.setField(cacheService, "adminRoleRelationMapper", adminRoleRelationMapper);
        ReflectionTestUtils.setField(cacheService, "adminRoleRelationDao", adminRoleRelationDao);
        ReflectionTestUtils.setField(cacheService, "REDIS_DATABASE", "mall");
        ReflectionTestUtils.setField(cacheService, "REDIS_EXPIRE", 3600L);
        ReflectionTestUtils.setField(cacheService, "REDIS_KEY_ADMIN", "admin");
        ReflectionTestUtils.setField(cacheService, "REDIS_KEY_RESOURCE_LIST", "resourceList");
    }

    @Test
    void setAndGetAdminUseStableAdminCacheKey() {
        UmsAdmin admin = admin(1L, "admin");
        when(redisService.get("mall:admin:admin")).thenReturn(admin);

        cacheService.setAdmin(admin);
        UmsAdmin cached = cacheService.getAdmin("admin");

        verify(redisService).set("mall:admin:admin", admin, 3600L);
        assertThat(cached).isSameAs(admin);
    }

    @Test
    void delResourceListByRoleDeletesEveryAffectedAdminResourceKey() {
        when(adminRoleRelationMapper.selectByExample(any())).thenReturn(List.of(relation(1L), relation(2L)));

        cacheService.delResourceListByRole(10L);

        verify(redisService).del(List.of("mall:resourceList:1", "mall:resourceList:2"));
    }

    @Test
    void delResourceListByResourceDeletesEveryAffectedAdminResourceKey() {
        when(adminRoleRelationDao.getAdminIdList(100L)).thenReturn(List.of(3L, 4L));

        cacheService.delResourceListByResource(100L);

        verify(redisService).del(List.of("mall:resourceList:3", "mall:resourceList:4"));
    }

    private static UmsAdmin admin(Long id, String username) {
        UmsAdmin admin = new UmsAdmin();
        admin.setId(id);
        admin.setUsername(username);
        return admin;
    }

    private static UmsAdminRoleRelation relation(Long adminId) {
        UmsAdminRoleRelation relation = new UmsAdminRoleRelation();
        relation.setAdminId(adminId);
        return relation;
    }
}
