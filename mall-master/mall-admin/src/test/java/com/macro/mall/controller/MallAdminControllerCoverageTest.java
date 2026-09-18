package com.macro.mall.controller;

import com.macro.mall.common.api.CommonResult;
import com.macro.mall.common.api.ResultCode;
import com.macro.mall.testsupport.TestSamples;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
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

import static org.assertj.core.api.Assertions.assertThat;

class MallAdminControllerCoverageTest {

    private static final List<Class<?>> CONTROLLERS = List.of(
            CmsPrefrenceAreaController.class,
            CmsSubjectController.class,
            OmsCompanyAddressController.class,
            OmsOrderController.class,
            OmsOrderReturnApplyController.class,
            OmsOrderReturnReasonController.class,
            OmsOrderSettingController.class,
            OssController.class,
            PmsBrandController.class,
            PmsProductAttributeCategoryController.class,
            PmsProductAttributeController.class,
            PmsProductCategoryController.class,
            PmsProductController.class,
            PmsSkuStockController.class,
            SmsCouponController.class,
            SmsCouponHistoryController.class,
            SmsFlashPromotionController.class,
            SmsFlashPromotionProductRelationController.class,
            SmsFlashPromotionSessionController.class,
            SmsHomeAdvertiseController.class,
            SmsHomeBrandController.class,
            SmsHomeNewProductController.class,
            SmsHomeRecommendProductController.class,
            SmsHomeRecommendSubjectController.class,
            UmsAdminController.class,
            UmsMemberLevelController.class,
            UmsMenuController.class,
            UmsResourceCategoryController.class,
            UmsResourceController.class,
            UmsRoleController.class
    );

    @TestFactory
    Stream<DynamicTest> controllerEntryPointsReturnCommonResult() {
        return CONTROLLERS.stream()
                .flatMap(controllerClass -> publicMethods(controllerClass)
                        .map(method -> DynamicTest.dynamicTest(
                                controllerClass.getSimpleName() + "." + method.getName() + signature(method),
                                () -> invokeControllerMethod(controllerClass, method))));
    }

    @Test
    void minioControllerReturnsFailedResultWhenEndpointConfigIsInvalid() {
        MinioController controller = new MinioController();
        ReflectionTestUtils.setField(controller, "ENDPOINT", "");
        ReflectionTestUtils.setField(controller, "BUCKET_NAME", "mall-test");
        ReflectionTestUtils.setField(controller, "ACCESS_KEY", "access");
        ReflectionTestUtils.setField(controller, "SECRET_KEY", "secret");

        CommonResult uploadResult = controller.upload((org.springframework.web.multipart.MultipartFile)
                TestSamples.sampleValue(org.springframework.web.multipart.MultipartFile.class));
        CommonResult deleteResult = controller.delete("test.jpg");

        assertThat(uploadResult.getCode()).isEqualTo(ResultCode.FAILED.getCode());
        assertThat(deleteResult.getCode()).isEqualTo(ResultCode.FAILED.getCode());
    }

    private static Stream<Method> publicMethods(Class<?> controllerClass) {
        return Arrays.stream(controllerClass.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .sorted(Comparator.comparing(Method::getName).thenComparing(MallAdminControllerCoverageTest::signature));
    }

    private static void invokeControllerMethod(Class<?> controllerClass, Method method) throws Exception {
        Object controller = controllerClass.getDeclaredConstructor().newInstance();
        injectControllerFields(controller);

        Object result;
        try {
            result = method.invoke(controller, TestSamples.argumentsFor(method));
        } catch (InvocationTargetException exception) {
            Throwable target = exception.getTargetException();
            if (target instanceof Exception targetException) {
                throw targetException;
            }
            throw exception;
        }

        assertThat(result).isInstanceOf(CommonResult.class);
        assertThat(((CommonResult<?>) result).getCode()).isEqualTo(ResultCode.SUCCESS.getCode());
    }

    private static void injectControllerFields(Object controller) {
        for (Field field : controller.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            Object value = valueForField(field);
            ReflectionTestUtils.setField(controller, field.getName(), value);
        }
    }

    private static Object valueForField(Field field) {
        Class<?> fieldType = field.getType();
        if (fieldType == String.class) {
            if ("tokenHeader".equals(field.getName())) {
                return "Authorization";
            }
            if ("tokenHead".equals(field.getName())) {
                return "Bearer ";
            }
            return "value";
        }
        if (fieldType.isPrimitive() || Number.class.isAssignableFrom(fieldType) || fieldType == Boolean.class) {
            return TestSamples.sampleValue(fieldType);
        }
        return TestSamples.mockWithSamples(fieldType);
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
