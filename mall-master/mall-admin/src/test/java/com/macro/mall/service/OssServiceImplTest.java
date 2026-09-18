package com.macro.mall.service;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.PolicyConditions;
import com.macro.mall.dto.OssCallbackResult;
import com.macro.mall.dto.OssPolicyResult;
import com.macro.mall.service.impl.OssServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OssServiceImplTest {

    private OSSClient ossClient;
    private OssServiceImpl ossService;

    @BeforeEach
    void setUp() {
        ossClient = mock(OSSClient.class, RETURNS_DEEP_STUBS);
        ossService = new OssServiceImpl();
        ReflectionTestUtils.setField(ossService, "ALIYUN_OSS_EXPIRE", 60);
        ReflectionTestUtils.setField(ossService, "ALIYUN_OSS_MAX_SIZE", 10);
        ReflectionTestUtils.setField(ossService, "ALIYUN_OSS_CALLBACK", "http://localhost/callback");
        ReflectionTestUtils.setField(ossService, "ALIYUN_OSS_BUCKET_NAME", "mall-bucket");
        ReflectionTestUtils.setField(ossService, "ALIYUN_OSS_ENDPOINT", "oss-cn-test.aliyuncs.com");
        ReflectionTestUtils.setField(ossService, "ALIYUN_OSS_DIR_PREFIX", "mall/");
        ReflectionTestUtils.setField(ossService, "ossClient", ossClient);
    }

    @Test
    void policyBuildsUploadPolicyResultFromOssClient() throws Exception {
        when(ossClient.generatePostPolicy(any(Date.class), any(PolicyConditions.class))).thenReturn("policy-json");
        when(ossClient.calculatePostSignature("policy-json")).thenReturn("signature");
        when(ossClient.getCredentialsProvider().getCredentials().getAccessKeyId()).thenReturn("access-key");

        OssPolicyResult result = ossService.policy();

        assertThat(result.getAccessKeyId()).isEqualTo("access-key");
        assertThat(result.getSignature()).isEqualTo("signature");
        assertThat(result.getPolicy()).isNotBlank();
        assertThat(result.getCallback()).isNotBlank();
        assertThat(result.getDir()).startsWith("mall/");
        assertThat(result.getHost()).isEqualTo("http://mall-bucket.oss-cn-test.aliyuncs.com");
    }

    @Test
    void callbackBuildsPublicFileUrlAndCopiesMetadata() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("filename", "mall/20260101/test.jpg");
        request.setParameter("size", "1024");
        request.setParameter("mimeType", "image/jpeg");
        request.setParameter("width", "800");
        request.setParameter("height", "600");

        OssCallbackResult result = ossService.callback(request);

        assertThat(result.getFilename()).isEqualTo("http://mall-bucket.oss-cn-test.aliyuncs.com/mall/20260101/test.jpg");
        assertThat(result.getSize()).isEqualTo("1024");
        assertThat(result.getMimeType()).isEqualTo("image/jpeg");
        assertThat(result.getWidth()).isEqualTo("800");
        assertThat(result.getHeight()).isEqualTo("600");
    }
}
