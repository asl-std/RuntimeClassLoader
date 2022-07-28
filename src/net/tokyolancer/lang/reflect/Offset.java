package net.tokyolancer.lang.reflect;

import net.tokyolancer.lang.exp.MultiKeyMap;

public class Offset {

    // -- Fields offsets -- //

    // Storage data like:
    // class name, field name, field offset
    // Contains only verified offsets
    private static final MultiKeyMap<String, String, Long> offsets = new MultiKeyMap<>();

    static {
        // Load all offsets for current JVM Version
        // Currently supports only Java SE 8 and Java 16, 17
        switch (Reflection.getRuntimeVersion() ) {
            case 8:
                offsets.put("java.lang.reflect.Method","modifiers", 36L);
                offsets.put("java.lang.reflect.Method", "clazz", 40L);
                offsets.put("java.lang.reflect.Method", "methodAccessor", 80L);
                // module missing
                offsets.put("java.lang.Class", "classLoader", 24L);
                offsets.put("java.lang.ClassLoader", "parent", 12L);
                offsets.put("java.lang.ClassLoader", "classes", 24L);
                break;
            case 16:
            case 17:
                offsets.put("java.lang.reflect.Method","modifiers", 32L);
                offsets.put("java.lang.reflect.Method", "clazz", 36L);
                offsets.put("java.lang.reflect.Method", "methodAccessor", 76L);
                offsets.put("java.lang.Class", "module", 48L);
                offsets.put("java.lang.Class", "classLoader", 52L);
                offsets.put("java.lang.ClassLoader", "parent", 24L);
                offsets.put("java.lang.ClassLoader", "classes", 48L);
                break;
        }
    }

    public static long of(Class<?> clazz, String fieldName) {
        return Offset.of(clazz.getSimpleName(), fieldName);
    }

    private static Long of(String className, String fieldName) {
        return Offset.offsets.get(className, fieldName);
    }
}
