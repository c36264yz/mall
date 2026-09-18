package com.macro.mall.service;

import com.macro.mall.dao.UmsAdminRoleRelationDao;
import com.macro.mall.dto.UmsAdminParam;
import com.macro.mall.dto.UpdateAdminPasswordParam;
import com.macro.mall.mapper.UmsAdminLoginLogMapper;
import com.macro.mall.mapper.UmsAdminMapper;
import com.macro.mall.mapper.UmsAdminRoleRelationMapper;
import com.macro.mall.model.UmsAdmin;
import com.macro.mall.model.UmsResource;
import com.macro.mall.security.util.JwtTokenUtil;
import com.macro.mall.service.impl.UmsAdminServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UmsAdminServiceImplTest {

    @Mock
    private JwtTokenUtil jwtTokenUtil;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UmsAdminMapper adminMapper;
    @Mock
    private UmsAdminRoleRelationMapper adminRoleRelationMapper;
    @Mock
    private UmsAdminRoleRelationDao adminRoleRelationDao;
    @Mock
    private UmsAdminLoginLogMapper loginLogMapper;
    @Mock
    private UmsAdminCacheService cacheService;

    private TestableUmsAdminServiceImpl adminService;

    @BeforeEach
    void setUp() {
        adminService = new TestableUmsAdminServiceImpl(cacheService);
        ReflectionTestUtils.setField(adminService, "jwtTokenUtil", jwtTokenUtil);
        ReflectionTestUtils.setField(adminService, "passwordEncoder", passwordEncoder);
        ReflectionTestUtils.setField(adminService, "adminMapper", adminMapper);
        ReflectionTestUtils.setField(adminService, "adminRoleRelationMapper", adminRoleRelationMapper);
        ReflectionTestUtils.setField(adminService, "adminRoleRelationDao", adminRoleRelationDao);
        ReflectionTestUtils.setField(adminService, "loginLogMapper", loginLogMapper);
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
        SecurityContextHolder.clearContext();
    }

    @Test
    void registerReturnsNullWhenUsernameAlreadyExists() {
        when(adminMapper.selectByExample(any())).thenReturn(List.of(admin("admin", "encoded")));

        UmsAdmin result = adminService.register(adminParam("admin", "raw"));

        assertThat(result).isNull();
        verify(adminMapper, never()).insert(any());
    }

    @Test
    void registerEncodesPasswordAndCreatesEnabledAdmin() {
        when(adminMapper.selectByExample(any())).thenReturn(Collections.emptyList());
        when(passwordEncoder.encode("raw")).thenReturn("encoded");

        UmsAdmin result = adminService.register(adminParam("admin", "raw"));

        assertThat(result.getUsername()).isEqualTo("admin");
        assertThat(result.getPassword()).isEqualTo("encoded");
        assertThat(result.getStatus()).isEqualTo(1);
        verify(adminMapper).insert(result);
    }

    @Test
    void loginGeneratesTokenAndWritesLoginLogWhenCredentialsMatch() {
        UmsAdmin admin = admin("admin", "encoded");
        when(cacheService.getAdmin("admin")).thenReturn(admin);
        when(cacheService.getResourceList(1L)).thenReturn(List.of(new UmsResource()));
        when(passwordEncoder.matches("raw", "encoded")).thenReturn(true);
        when(jwtTokenUtil.generateToken(any())).thenReturn("jwt-token");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));

        String token = adminService.login("admin", "raw");

        assertThat(token).isEqualTo("jwt-token");
        verify(loginLogMapper).insert(any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }

    @Test
    void updatePasswordReturnsStatusCodesForInvalidAndValidChanges() {
        assertThat(adminService.updatePassword(new UpdateAdminPasswordParam())).isEqualTo(-1);

        UpdateAdminPasswordParam param = updatePasswordParam();
        when(adminMapper.selectByExample(any())).thenReturn(Collections.emptyList());
        assertThat(adminService.updatePassword(param)).isEqualTo(-2);

        UmsAdmin admin = admin("admin", "encoded");
        when(adminMapper.selectByExample(any())).thenReturn(List.of(admin));
        when(passwordEncoder.matches("old", "encoded")).thenReturn(false, true);
        assertThat(adminService.updatePassword(param)).isEqualTo(-3);

        when(passwordEncoder.encode("new")).thenReturn("new-encoded");
        assertThat(adminService.updatePassword(param)).isEqualTo(1);
        assertThat(admin.getPassword()).isEqualTo("new-encoded");
        verify(adminMapper).updateByPrimaryKey(admin);
        verify(cacheService).delAdmin(1L);
    }

    @Test
    void updateRoleReplacesRelationsAndClearsResourceCache() {
        int count = adminService.updateRole(1L, List.of(10L, 20L));

        assertThat(count).isEqualTo(2);
        verify(adminRoleRelationMapper).deleteByExample(any());
        verify(adminRoleRelationDao).insertList(anyList());
        verify(cacheService).delResourceList(1L);
    }

    private static UmsAdminParam adminParam(String username, String password) {
        UmsAdminParam param = new UmsAdminParam();
        param.setUsername(username);
        param.setPassword(password);
        param.setNickName("Admin");
        return param;
    }

    private static UpdateAdminPasswordParam updatePasswordParam() {
        UpdateAdminPasswordParam param = new UpdateAdminPasswordParam();
        param.setUsername("admin");
        param.setOldPassword("old");
        param.setNewPassword("new");
        return param;
    }

    private static UmsAdmin admin(String username, String password) {
        UmsAdmin admin = new UmsAdmin();
        admin.setId(1L);
        admin.setUsername(username);
        admin.setPassword(password);
        admin.setStatus(1);
        return admin;
    }

    private static class TestableUmsAdminServiceImpl extends UmsAdminServiceImpl {
        private final UmsAdminCacheService cacheService;

        private TestableUmsAdminServiceImpl(UmsAdminCacheService cacheService) {
            this.cacheService = cacheService;
        }

        @Override
        public UmsAdminCacheService getCacheService() {
            return cacheService;
        }
    }
}
