package dev.voxellink.util;

import java.lang.reflect.Field;

public class ReflectionUtil {
    public static Object findFieldByType(Object instance, String type) {
        if (instance == null || type == null) {
            return null;
        }
        Class<?> clazz = instance.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (!field.getType().getName().endsWith(type)) {
                continue;
            }
            field.setAccessible(true);
            try {
                return field.get(instance);
            } catch (IllegalAccessException e) {
                return null;
            }
        }
        return null;
    }
}
