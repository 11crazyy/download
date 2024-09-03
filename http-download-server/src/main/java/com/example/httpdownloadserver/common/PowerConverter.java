package com.example.httpdownloadserver.common;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.util.TypeUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.util.Assert;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

public class PowerConverter {

    /**
     * 对象转化失败
     */
    private static final Logger LOGGER = LogManager.getLogger(PowerConverter.class);

    /**
     * 包装类型映射
     */
    private static final Map<Class<?>,Class<?>> WRAP_TYPE_MAP = new IdentityHashMap<>();

    /**
     * 可转换单一类型映射表
     * todo 当前暂不支持接口类型的注册
     */
    private static final Map<Pair<Class<?>,Class<?>>,Function<?,?>> SIMPLE_CONVERTIBLE_MAP = new HashMap<>();

    static {
        WRAP_TYPE_MAP.put(int.class, Integer.class);
        WRAP_TYPE_MAP.put(short.class, Short.class);
        WRAP_TYPE_MAP.put(long.class, Long.class);
        WRAP_TYPE_MAP.put(float.class, Float.class);
        WRAP_TYPE_MAP.put(double.class, Double.class);
        WRAP_TYPE_MAP.put(boolean.class, Boolean.class);
        WRAP_TYPE_MAP.put(char.class, Character.class);
        WRAP_TYPE_MAP.put(byte.class, Byte.class);

        registerConverter(Number.class, String.class, Object::toString);
        registerConverter(Boolean.class, String.class, Object::toString);
        registerConverter(BigDecimal.class, String.class, BigDecimal::toPlainString);
        registerConverter(Boolean.class, Byte.class, e -> e ? (byte) 1 : (byte) 0);
        registerConverter(Boolean.class, Integer.class, e -> e ? 1 : 0);
        registerConverter(Integer.class, Byte.class, Integer::byteValue);
        registerConverter(Byte.class, Integer.class, Byte::intValue);
        registerConverter(Number.class, Boolean.class, e -> e.longValue() != 0);
        registerConverter(String.class, Boolean.class, Boolean::parseBoolean);
    }

    /**
     * 注册转化器
     *
     * @param srcType  原类型
     * @param dstType  目标类型
     * @param function 转化方法
     * @param <T>      原类型
     * @param <U>      目标类型
     */
    public static <T, U> void registerConverter(Class<T> srcType, Class<U> dstType, Function<T,U> function) {
        SIMPLE_CONVERTIBLE_MAP.put(new Pair<>(srcType, dstType), function);
    }

    /**
     * 匹配转化器
     *
     * @param srcType 原类型
     * @param dstType 目标类型
     * @return 转化器
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    static Function<?,?> matchConverter(Class<?> srcType, Class<?> dstType) {
        Class<?> matchingType = srcType;
        while (matchingType != Object.class) {
            Function<?,?> function = SIMPLE_CONVERTIBLE_MAP.get(new Pair(matchingType, dstType));
            if (function != null) {
                return function;
            }
            for (Class<?> anInterface : matchingType.getInterfaces()) {
                function = SIMPLE_CONVERTIBLE_MAP.get(new Pair(anInterface, dstType));
                if (function != null) {
                    return function;
                }
            }
            matchingType = matchingType.getSuperclass();
        }
        return null;
    }

    /**
     * 转换基本对象
     *
     * @param source 原对象
     * @param t 目标类
     * @return 转换后的对象
     * @param <T> 转换类型
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T> T convertSimple(Object source, Class<?> t) {
        if (source == null) {
            return null;
        }
        Function function = matchConverter(source.getClass(), t);
        if (function == null) {
            return null;
        }
        return (T) function.apply(source);
    }

    /**
     * 列表转换为string
     *
     * @param list 列表
     * @return ,分隔的string
     * @param <T> 元素类型
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T> String toJoinedString(List<T> list) {
        StringJoiner stringJoiner = new StringJoiner(",");
        for (T t : list) {
            Function function = matchConverter(t.getClass(), String.class);
            Assert.notNull(function, "无法转换" + t.getClass());
            String result = (String) function.apply(t);
            stringJoiner.add(result);
        }
        return stringJoiner.toString();
    }

    /**
     * string转换为列表
     *
     * @param s string
     * @param t 元素类型
     * @return 列表
     * @param <T> 元素类型
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T> List<T> fromJoinedString(String s, Class<T> t) {
        Function function = matchConverter(String.class, t);
        Assert.notNull(function, "无法转换" + t);
        List<T> list = new ArrayList<>();
        for (String string : s.split(",")) {
            list.add(((T) function.apply(string)));
        }
        return list;
    }

    /**
     * 转化对象。可用于do、model、vo的转化，支持函数式编程
     *
     * @param source 来源对象
     * @param t      泛型
     * @param <T>    要转化的对象类型
     * @return 转化后的对象
     */
    public static <T> T convert(Object source, Class<T> t) {
        if (source == null) {
            return null;
        }
        return convertNonNull(source, t);
    }

    /**
     * 转化对象。可用于do、model、vo的转化
     *
     * @param source 来源对象
     * @param t      泛型
     * @param <T>    要转化的对象类型
     * @return 转化后的对象
     */
    public static <T> T convertNonNull(Object source, Class<T> t) {
        try {
            Assert.notNull(source, "转换对象不能为空,source=" + source.toString());
            Constructor<T> constructor = t.getConstructor();
            constructor.setAccessible(true);
            T target = constructor.newInstance();
            copy(source, target);
            return target;
        } catch (NoSuchMethodException e) {
            LOGGER.error("目标类不存在无参构造：" + t, e);
            throw new UnsupportedOperationException("目标类不存在无参构造：" + t, e);
        } catch (Exception e) {
            LOGGER.error("对象转化失败。原始对象:{}，目标对象class:{}", source, t);
            throw new UnsupportedOperationException(String.format("对象转化失败。原始对象:%s，目标对象class:%s", source, t), e);
        }
    }

    /**
     * 复制对象，只针对同名同类型的字段进行值拷贝
     *
     * @param source 来源对象
     * @param target 目标对象
     */
    @SuppressWarnings({"unchecked", "null", "rawtypes"})
    public static void copy(Object source, Object target) {

        try {
            BeanCopier copier = BeanCopier.create(source.getClass(), target.getClass(), true);
            // 由于方法签名的限制，sourceFieldValue必定为包装类型
            copier.copy(source, target, (sourceFieldValue, targetFieldClass, setMethod) -> {

                // 1. 如果原始值为空则赋null值
                if (sourceFieldValue == null) {
                    return null;
                }

                Class<?> sourceFieldClass = sourceFieldValue.getClass();

                // 2. 如果类型一样的，那就直接返回，但是Collection除外
                if (targetFieldClass.isInstance(sourceFieldValue)) {
                    if (Collection.class.isAssignableFrom(targetFieldClass)) {
                        // 转list对象
                        return convertCollection(sourceFieldValue, target, setMethod.toString());

                    }
                    return sourceFieldValue;
                }

                // 3. 如果是基础类型，直接返回
                if (sourceFieldClass == getWrappedType(targetFieldClass)) {
                    return sourceFieldValue;
                }
                // 4. 自动匹配
                Function matchedConverter = matchConverter(sourceFieldClass, getWrappedType(targetFieldClass));
                if (matchedConverter != null) {
                    return matchedConverter.apply(sourceFieldValue);
                }

                // 6. String与List<String>转化，逗号分隔
                if (sourceFieldValue instanceof List list && String.class == targetFieldClass) {
                    StringJoiner stringJoiner = new StringJoiner(",");
                    for (Object o : list) {
                        stringJoiner.add(TypeUtils.cast(o, String.class));
                    }
                    return stringJoiner.toString();
                }
                if (sourceFieldValue instanceof String && List.class.isAssignableFrom(targetFieldClass)) {
                    return Arrays.asList(((String) sourceFieldValue).split(","));
                }
                // Map类型转为String
                if (String.class == targetFieldClass && sourceFieldValue instanceof Map) {
                    return JSON.toJSONString(sourceFieldValue);
                }
                // String转Map类型
                if (sourceFieldValue instanceof String && Map.class.isAssignableFrom(targetFieldClass)) {
                    return JSON.parseObject((String) sourceFieldValue, targetFieldClass);
                }
                // 7. 复杂类型转化（递归）。同时做下保护，只转化应用内的复杂类型
                if (isComplexCls(sourceFieldClass) && isComplexCls(targetFieldClass)) {
                    return convertNonNull(sourceFieldValue, (Class<?>) targetFieldClass);
                }
                return null;
            });
        } catch (Exception e) {
            LOGGER.error("对象转化失败。原始对象:{}，目标对象:{}", source, target);
            throw e;
        }
    }

    /**
     * 从set方法获取属性名
     *
     * @param setMethod set方法名
     * @return 属性名
     */
    private static String getFieldNameFromSetter(String setMethod) {
        return setMethod.substring(3, 4).toLowerCase(Locale.ROOT) + setMethod.substring(4);
    }

    /**
     * 是否为复杂类型
     *
     * @param cls 类型
     * @return true/false 1/0
     */
    private static boolean isComplexCls(Class<?> cls) {
        return cls.getName().startsWith("wiki.moe.kumiko");
    }

    /**
     * list 和 set转换
     *
     * @param sourceFieldValue 源对象字段值
     * @param target           目标对象
     * @param setMethod        set方法
     * @return 转换后的对象
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Collection<Object> convertCollection(Object sourceFieldValue, Object target, String setMethod) {
        Method[] methods = target.getClass().getMethods();
        Method method = Arrays.stream(methods)
                .filter(method1 -> method1.getName().equals(setMethod))
                .findFirst()
                .orElse(null);

        if (method == null) {
            return null;
        }
        Collection<Object> list;
        if (sourceFieldValue instanceof List<?>) {
            list = new ArrayList<>();
        }
        else {
            list = new HashSet<>();
        }

        // 找到set入参
        Type type = (method.getGenericParameterTypes())[0];

        if (type instanceof ParameterizedType) {
            //获取泛型参数类型的实际参数类型
            Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
            Class<?> c1 = (Class<?>) actualTypeArguments[0];
            Collection<?> sourceCollection = (Collection<?>) sourceFieldValue;
            if (sourceCollection.isEmpty()) {
                return list;
            }
            Object next = sourceCollection.iterator().next();
            Class<?> sourceType = next.getClass();
            if (sourceType.isEnum()) {
                list.addAll(sourceCollection);
            }
            else if (isComplexCls(sourceType) && isComplexCls(c1)) {
                for (Object o : sourceCollection) {
                    list.add(PowerConverter.convert(o, c1));
                }
            }
            else if (c1.isAssignableFrom(sourceType)) {
                list.addAll(sourceCollection);
            }
            else {
                Function function = matchConverter(sourceType, c1);
                if (function != null) {
                    for (Object o : sourceCollection) {
                        list.add(function.apply(o));
                    }
                }
            }
        }
        return list;
    }

    /**
     * 获取包装类型
     *
     * @param type 基本或包装类型
     * @return 包装类型
     */
    private static Class<?> getWrappedType(Class<?> type) {
        Assert.notNull(type, "类型为空");
        if (type.isPrimitive()) {
            return WRAP_TYPE_MAP.get(type);
        }
        return type;
    }

    /**
     * 批量转化对象类型
     *
     * @param source 对象
     * @param t      泛型
     * @param <T>    要转化的对象类型
     * @return 批量转化对象
     */
    public static <T> List<T> batchConvert(List<?> source, Class<T> t) {
        if (CollectionUtils.isEmpty(source)) {
            return new ArrayList<>();
        }
        List<T> list = new ArrayList<>();
        for (Object it : source) {
            T convert = convert(it, t);
            list.add(convert);
        }
        return list;
    }
}
