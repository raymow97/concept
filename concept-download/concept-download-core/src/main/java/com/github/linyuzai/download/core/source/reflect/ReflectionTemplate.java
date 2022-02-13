package com.github.linyuzai.download.core.source.reflect;

import com.github.linyuzai.download.core.exception.DownloadException;
import com.github.linyuzai.download.core.source.Source;
import com.github.linyuzai.download.core.source.reflect.conversion.ValueConversion;
import com.github.linyuzai.download.core.source.reflect.conversion.ValueConvertor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 每个类对应一个反射模版，包含该类的反射信息。
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReflectionTemplate {

    protected final Map<Class<? extends Annotation>, Reflector> reflectorMap = new HashMap<>();

    public ReflectionTemplate(Class<?> clazz) {
        this(clazz, isReflectSuper(clazz));
    }

    /**
     * 处理标记了 {@link SourceReflection} 注解的方法或字段
     *
     * @param clazz        Class
     * @param reflectSuper 是否反射父类
     */
    public ReflectionTemplate(Class<?> clazz, boolean reflectSuper) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            Annotation[] annotations = method.getAnnotations();
            for (Annotation annotation : annotations) {
                Class<? extends Annotation> annotationType = annotation.annotationType();
                if (annotationType.isAnnotationPresent(SourceReflection.class)) {
                    int count = method.getParameterCount();
                    if (count == 0) {
                        Reflector exist = reflectorMap.get(annotationType);
                        if (exist == null) {
                            reflectorMap.put(annotationType, new MethodReflector(method));
                        } else {
                            throw new DownloadException(annotationType.getName()
                                    + " is already defined on " + exist);
                        }
                    } else {
                        throw new DownloadException("Method cannot have parameters: " + method);
                    }
                }
            }
        }
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            Annotation[] annotations = field.getAnnotations();
            for (Annotation annotation : annotations) {
                Class<? extends Annotation> annotationType = annotation.annotationType();
                if (annotationType.isAnnotationPresent(SourceReflection.class)) {
                    Reflector exist = reflectorMap.get(annotationType);
                    if (exist == null) {
                        reflectorMap.put(annotationType, new FieldReflector(field));
                    } else {
                        throw new DownloadException(annotationType.getName()
                                + " is already defined on " + exist);
                    }
                }
            }
        }

        if (reflectSuper) {
            Class<?> superclass = clazz.getSuperclass();
            if (superclass != null) {
                ReflectionTemplate template = new ReflectionTemplate(superclass, true);
                mergeSuper(template);
            }
        }
    }

    @Deprecated
    public static boolean hasAnnotation(Class<? extends Annotation> annotation) {
        if (annotation == SourceReflection.class) {
            return true;
        }
        Annotation[] annotations = annotation.getAnnotations();
        for (Annotation a : annotations) {
            boolean hasAnnotation = hasAnnotation(a.annotationType());
            if (hasAnnotation) {
                return true;
            }
        }
        return false;
    }

    public static boolean isReflectSuper(Class<?> clazz) {
        SourceModel annotation = clazz.getAnnotation(SourceModel.class);
        if (annotation == null) {
            throw new DownloadException("@SourceModel not found");
        }
        return annotation.superclass();
    }

    /**
     * 通过注解获得值。
     *
     * @param annotation Annotation
     * @param model      模型
     * @return 值
     */
    public Object value(Class<? extends Annotation> annotation, Object model) {
        return reflectorMap.get(annotation).reflect(model);
    }

    /**
     * 将模型的值通过反射设置到 {@link Source}，
     * 如果值类型不匹配则尝试使用 {@link ValueConvertor} 进行值转换。
     *
     * @param model  模型
     * @param source {@link Source}
     */
    @SneakyThrows
    public void reflect(Object model, Source source) {
        for (Map.Entry<Class<? extends Annotation>, Reflector> entry : reflectorMap.entrySet()) {
            SourceReflection reflection = entry.getKey().getAnnotation(SourceReflection.class);
            String methodName = reflection.methodName();
            if (methodName.isEmpty()) {
                String fieldName = reflection.fieldName();
                if (fieldName.isEmpty()) {
                    //Ignore SourceObject
                } else {
                    Object reflect = entry.getValue().reflect(model);
                    if (reflect == null) {
                        continue;
                    }
                    Field field = getReflectField(source.getClass(), fieldName);
                    if (field == null) {
                        throw new NoSuchFieldException(fieldName);
                    }
                    Class<?> type = field.getType();
                    Object value;
                    if (type.isInstance(reflect)) {
                        value = reflect;
                    } else {
                        value = convertValue(reflect, type);
                    }
                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }
                    field.set(source, value);
                }
            } else {
                Object reflect = entry.getValue().reflect(model);
                if (reflect == null) {
                    continue;
                }
                Class<?> parameterType = reflection.methodParameterType();
                Object value;
                if (parameterType.isInstance(reflect)) {
                    value = reflect;
                } else {
                    value = convertValue(reflect, parameterType);
                }
                Method method = getReflectMethod(source.getClass(), methodName, parameterType);
                if (method == null) {
                    throw new NoSuchMethodException(methodName);
                }
                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }
                method.invoke(source, value);
            }
        }
    }

    protected Field getReflectField(Class<?> clazz, String name) {
        Class<?> c = clazz;
        while (c != null) {
            try {
                return c.getDeclaredField(name);
            } catch (Throwable ignore) {
            }
            c = c.getSuperclass();
        }
        return null;
    }

    protected Method getReflectMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
        Class<?> c = clazz;
        while (c != null) {
            try {
                return c.getDeclaredMethod(name, parameterTypes);
            } catch (Throwable ignore) {
            }
            c = c.getSuperclass();
        }
        return null;
    }

    protected Object convertValue(Object value, Class<?> type) {
        return ValueConversion.getInstance().convert(value, type);
    }

    /**
     * 合并父类模版的反射信息，
     * 即支持父类上的注解。
     *
     * @param template 父类模版
     */
    protected void mergeSuper(ReflectionTemplate template) {
        for (Map.Entry<Class<? extends Annotation>, Reflector> entry : template.reflectorMap.entrySet()) {
            if (!reflectorMap.containsKey(entry.getKey())) {
                reflectorMap.put(entry.getKey(), entry.getValue());
            }
        }
    }
}
