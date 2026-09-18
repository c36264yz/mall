package com.macro.mall.service;

import com.macro.mall.common.service.RedisService;
import com.macro.mall.service.impl.CmsPrefrenceAreaServiceImpl;
import com.macro.mall.service.impl.CmsSubjectServiceImpl;
import com.macro.mall.service.impl.OmsCompanyAddressServiceImpl;
import com.macro.mall.service.impl.OmsOrderReturnApplyServiceImpl;
import com.macro.mall.service.impl.OmsOrderReturnReasonServiceImpl;
import com.macro.mall.service.impl.OmsOrderServiceImpl;
import com.macro.mall.service.impl.OmsOrderSettingServiceImpl;
import com.macro.mall.service.impl.PmsBrandServiceImpl;
import com.macro.mall.service.impl.PmsProductAttributeCategoryServiceImpl;
import com.macro.mall.service.impl.PmsProductAttributeServiceImpl;
import com.macro.mall.service.impl.PmsProductCategoryServiceImpl;
import com.macro.mall.service.impl.PmsProductServiceImpl;
import com.macro.mall.service.impl.PmsSkuStockServiceImpl;
import com.macro.mall.service.impl.SmsCouponHistoryServiceImpl;
import com.macro.mall.service.impl.SmsCouponServiceImpl;
import com.macro.mall.service.impl.SmsFlashPromotionProductRelationServiceImpl;
import com.macro.mall.service.impl.SmsFlashPromotionServiceImpl;
import com.macro.mall.service.impl.SmsFlashPromotionSessionServiceImpl;
import com.macro.mall.service.impl.SmsHomeAdvertiseServiceImpl;
import com.macro.mall.service.impl.SmsHomeBrandServiceImpl;
import com.macro.mall.service.impl.SmsHomeNewProductServiceImpl;
import com.macro.mall.service.impl.SmsHomeRecommendProductServiceImpl;
import com.macro.mall.service.impl.SmsHomeRecommendSubjectServiceImpl;
import com.macro.mall.service.impl.UmsAdminCacheServiceImpl;
import com.macro.mall.service.impl.UmsMemberLevelServiceImpl;
import com.macro.mall.service.impl.UmsMenuServiceImpl;
import com.macro.mall.service.impl.UmsResourceCategoryServiceImpl;
import com.macro.mall.service.impl.UmsResourceServiceImpl;
import com.macro.mall.service.impl.UmsRoleServiceImpl;
import com.macro.mall.testsupport.TestSamples;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatCode;

class MallAdminServiceSmokeCoverageTest {

    private static final List<Class<?>> SERVICE_IMPLEMENTATIONS = List.of(
            CmsPrefrenceAreaServiceImpl.class,
            CmsSubjectServiceImpl.class,
            OmsCompanyAddressServiceImpl.class,
            OmsOrderReturnApplyServiceImpl.class,
            OmsOrderReturnReasonServiceImpl.class,
            OmsOrderServiceImpl.class,
            OmsOrderSettingServiceImpl.class,
            PmsBrandServiceImpl.class,
            PmsProductAttributeCategoryServiceImpl.class,
            PmsProductAttributeServiceImpl.class,
            PmsProductCategoryServiceImpl.class,
            PmsProductServiceImpl.class,
            PmsSkuStockServiceImpl.class,
            SmsCouponHistoryServiceImpl.class,
            SmsCouponServiceImpl.class,
            SmsFlashPromotionProductRelationServiceImpl.class,
            SmsFlashPromotionServiceImpl.class,
            SmsFlashPromotionSessionServiceImpl.class,
            SmsHomeAdvertiseServiceImpl.class,
            SmsHomeBrandServiceImpl.class,
            SmsHomeNewProductServiceImpl.class,
            SmsHomeRecommendProductServiceImpl.class,
            SmsHomeRecommendSubjectServiceImpl.class,
            UmsAdminCacheServiceImpl.class,
            UmsMemberLevelServiceImpl.class,
            UmsMenuServiceImpl.class,
            UmsResourceCategoryServiceImpl.class,
            UmsResourceServiceImpl.class,
            UmsRoleServiceImpl.class
    );

    @TestFactory
    Stream<DynamicTest> serviceMethodsRunWithMockedDependencies() {
        return SERVICE_IMPLEMENTATIONS.stream()
                .flatMap(serviceClass -> publicMethods(serviceClass)
                        .map(method -> DynamicTest.dynamicTest(
                                serviceClass.getSimpleName() + "." + method.getName() + signature(method),
                                () -> assertThatCode(() -> invokeServiceMethod(serviceClass, method)).doesNotThrowAnyException())));
    }

    private static Stream<Method> publicMethods(Class<?> serviceClass) {
        return Arrays.stream(serviceClass.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> !method.isBridge())
                .filter(method -> !method.isSynthetic())
                .sorted(Comparator.comparing(Method::getName).thenComparing(MallAdminServiceSmokeCoverageTest::signature));
    }

    private static void invokeServiceMethod(Class<?> serviceClass, Method method) throws Exception {
        Object service = serviceClass.getDeclaredConstructor().newInstance();
        injectFields(service);
        try {
            method.invoke(service, TestSamples.argumentsFor(method));
        } catch (InvocationTargetException exception) {
            Throwable target = exception.getTargetException();
            if (target instanceof Exception targetException) {
                throw targetException;
            }
            throw exception;
        }
    }

    private static void injectFields(Object service) {
        for (Field field : service.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            ReflectionTestUtils.setField(service, field.getName(), valueForField(field));
        }
    }

    private static Object valueForField(Field field) {
        Class<?> type = field.getType();
        if (type == RedisService.class) {
            return TestSamples.redisServiceMock();
        }
        if (type == String.class) {
            return field.getName().toLowerCase().contains("resource") ? "resourceList" : "mall";
        }
        if (type == Long.class || type == Long.TYPE) {
            return 3600L;
        }
        if (type == Integer.class || type == Integer.TYPE) {
            return 1;
        }
        if (type == Boolean.class || type == Boolean.TYPE) {
            return true;
        }
        return TestSamples.mockWithSamples(type);
    }

    private static String signature(Method method) {
        StringBuilder builder = new StringBuilder("(");
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            if (i > 0) {
                builder.append(",");
            }
            builder.append(parameterTypes[i].getSimpleName());
        }
        return builder.append(")").toString();
    }
}
