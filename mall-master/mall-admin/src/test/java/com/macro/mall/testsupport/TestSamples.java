package com.macro.mall.testsupport;

import com.macro.mall.common.service.RedisService;
import com.macro.mall.model.UmsAdmin;
import com.macro.mall.model.UmsResource;
import com.macro.mall.model.UmsRole;
import jakarta.servlet.http.HttpServletRequest;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public final class TestSamples {

    public static final Answer<Object> DEFAULT_ANSWER = invocation -> sampleValue(invocation.getMethod().getGenericReturnType());

    private TestSamples() {
    }

    public static Object[] argumentsFor(Method method) {
        Type[] types = method.getGenericParameterTypes();
        Object[] args = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            args[i] = sampleValue(types[i]);
        }
        return args;
    }

    public static Object sampleValue(Type type) {
        return sampleValue(type, new HashSet<>(), 0);
    }

    public static <T> T sampleObject(Class<T> type) {
        return type.cast(sampleValue(type));
    }

    public static <T> T mockWithSamples(Class<T> type) {
        return Mockito.mock(type, DEFAULT_ANSWER);
    }

    public static RedisService redisServiceMock() {
        RedisService redisService = Mockito.mock(RedisService.class, DEFAULT_ANSWER);
        when(redisService.get(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0, String.class);
            if (key != null && key.toLowerCase().contains("resource")) {
                return List.of(sampleObject(UmsResource.class));
            }
            return sampleObject(UmsAdmin.class);
        });
        return redisService;
    }

    private static Object sampleValue(Type type, Set<Class<?>> visiting, int depth) {
        if (type instanceof ParameterizedType parameterizedType) {
            Type rawType = parameterizedType.getRawType();
            if (rawType instanceof Class<?> rawClass) {
                if (Collection.class.isAssignableFrom(rawClass)) {
                    List<Object> list = new ArrayList<>();
                    Type[] actualTypes = parameterizedType.getActualTypeArguments();
                    if (actualTypes.length > 0) {
                        list.add(sampleValue(actualTypes[0], visiting, depth + 1));
                    }
                    return list;
                }
                if (Map.class.isAssignableFrom(rawClass)) {
                    Map<Object, Object> map = new LinkedHashMap<>();
                    Type[] actualTypes = parameterizedType.getActualTypeArguments();
                    Object key = actualTypes.length > 0 ? sampleValue(actualTypes[0], visiting, depth + 1) : "key";
                    Object value = actualTypes.length > 1 ? sampleValue(actualTypes[1], visiting, depth + 1) : "value";
                    map.put(key, value);
                    return map;
                }
                if (Optional.class.isAssignableFrom(rawClass)) {
                    Type[] actualTypes = parameterizedType.getActualTypeArguments();
                    return Optional.ofNullable(actualTypes.length > 0 ? sampleValue(actualTypes[0], visiting, depth + 1) : "value");
                }
                return sampleClass(rawClass, visiting, depth);
            }
        }
        if (type instanceof Class<?> clazz) {
            return sampleClass(clazz, visiting, depth);
        }
        return null;
    }

    private static Object sampleClass(Class<?> clazz, Set<Class<?>> visiting, int depth) {
        if (clazz == Void.TYPE || clazz == Void.class) {
            return null;
        }
        if (clazz == String.class) {
            return "value";
        }
        if (clazz == Long.TYPE || clazz == Long.class) {
            return 1L;
        }
        if (clazz == Integer.TYPE || clazz == Integer.class) {
            return 1;
        }
        if (clazz == Boolean.TYPE || clazz == Boolean.class) {
            return true;
        }
        if (clazz == Double.TYPE || clazz == Double.class) {
            return 1.0D;
        }
        if (clazz == Float.TYPE || clazz == Float.class) {
            return 1.0F;
        }
        if (clazz == Short.TYPE || clazz == Short.class) {
            return (short) 1;
        }
        if (clazz == Byte.TYPE || clazz == Byte.class) {
            return (byte) 1;
        }
        if (clazz == Character.TYPE || clazz == Character.class) {
            return 'a';
        }
        if (clazz == BigDecimal.class) {
            return BigDecimal.ONE;
        }
        if (clazz == Date.class) {
            return new Date();
        }
        if (clazz.isEnum()) {
            Object[] constants = clazz.getEnumConstants();
            return constants.length > 0 ? constants[0] : null;
        }
        if (clazz.isArray()) {
            return java.lang.reflect.Array.newInstance(clazz.getComponentType(), 0);
        }
        if (clazz == Principal.class) {
            Principal principal = Mockito.mock(Principal.class);
            when(principal.getName()).thenReturn("admin");
            return principal;
        }
        if (HttpServletRequest.class.isAssignableFrom(clazz)) {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer token");
            request.setParameter("filename", "test.jpg");
            request.setParameter("size", "10");
            request.setParameter("mimeType", "image/jpeg");
            request.setParameter("width", "100");
            request.setParameter("height", "100");
            return request;
        }
        if (MultipartFile.class.isAssignableFrom(clazz)) {
            MultipartFile file = Mockito.mock(MultipartFile.class);
            try {
                when(file.getOriginalFilename()).thenReturn("test.jpg");
                when(file.getContentType()).thenReturn("image/jpeg");
                when(file.getSize()).thenReturn(1L);
                when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{1}));
            } catch (Exception ignored) {
            }
            return file;
        }
        if (Collection.class.isAssignableFrom(clazz)) {
            return new ArrayList<>();
        }
        if (Map.class.isAssignableFrom(clazz)) {
            return new HashMap<>();
        }
        if (clazz == Object.class) {
            return new Object();
        }
        if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
            return Mockito.mock(clazz, DEFAULT_ANSWER);
        }
        if (clazz.getName().startsWith("java.") || clazz.getName().startsWith("jakarta.")) {
            return null;
        }
        if (depth > 3 || visiting.contains(clazz)) {
            return instantiate(clazz);
        }

        Object instance = instantiate(clazz);
        if (instance == null) {
            return null;
        }
        visiting.add(clazz);
        populateSetters(instance, visiting, depth + 1);
        applyDomainDefaults(instance);
        visiting.remove(clazz);
        return instance;
    }

    private static Object instantiate(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static void populateSetters(Object instance, Set<Class<?>> visiting, int depth) {
        Arrays.stream(instance.getClass().getMethods())
                .filter(method -> method.getName().startsWith("set"))
                .filter(method -> method.getParameterCount() == 1)
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .forEach(method -> {
                    Object value = sampleValue(method.getGenericParameterTypes()[0], visiting, depth);
                    if (value == null && method.getParameterTypes()[0].isPrimitive()) {
                        value = sampleValue(method.getParameterTypes()[0], visiting, depth);
                    }
                    try {
                        method.invoke(instance, value);
                    } catch (Exception ignored) {
                    }
                });
    }

    private static void applyDomainDefaults(Object instance) {
        if (instance instanceof UmsAdmin admin) {
            admin.setId(1L);
            admin.setUsername("admin");
            admin.setPassword("encoded");
            admin.setNickName("Admin");
            admin.setIcon("icon.png");
            admin.setStatus(1);
        }
        if (instance instanceof UmsRole role) {
            role.setId(1L);
            role.setName("ADMIN");
        }
        if (instance instanceof UmsResource resource) {
            resource.setId(1L);
            resource.setName("resource");
            resource.setUrl("/resource");
        }
    }
}
