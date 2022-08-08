package ru.aslcraft.runtimeclassloader.api.reflect;

import ru.aslcraft.runtimeclassloader.api.util.MultiKeyMap;

final class Offset {

    private Offset() { }

    // -- Fields offsets -- //

    // Хранит информацию в виде:
    // Название класса, Название поля в классе, Оффсет в памяти
    private static final MultiKeyMap<String, String, Long> offsets = new MultiKeyMap<>(7);

    static {
        // Подгружает оффсеты объектов в памяти для текущей версии JVM.
        // Поддерживает только Java 8, 16, 17
        switch (ReflectionImpl.getRuntimeVersion() ) {
            case 8:
                offsets.put("java.lang.reflect.Method","modifiers", 36L);
                offsets.put("java.lang.reflect.Method", "clazz", 40L);
                offsets.put("java.lang.reflect.Method", "methodAccessor", 80L);
                // Здесь оффсета на модуль нет, потому что его ещё не добавили.
                // P.S. Из-за этого вылилась огромная херня в классе Reflection
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

    /**
     *
     * Gets the offset of the required field in the specified class.
     * If the field is not indicated in the list of current offsets,
     * the NullPointerException error will be thrown and the execution
     * of the function (which uses the current method) will be interrupted
     * before the JVM is disconnected due to a non-existent address in memory.
     *
     * @param clazz The class from which to get the offset of the required field
     * @param fieldName The exact field name from which offset will be fetched
     * @return Current JVM offset of the field
     */
    public static long of(Class<?> clazz, String fieldName) {
        return Offset.of0(clazz.getSimpleName(), fieldName);
    }

    private static Long of0(String className, String fieldName) {
        return Offset.offsets.get(className, fieldName);
    }
}
