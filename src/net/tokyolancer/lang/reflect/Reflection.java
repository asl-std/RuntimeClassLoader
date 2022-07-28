package net.tokyolancer.lang.reflect;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static sun.misc.Unsafe.getUnsafe;

@SuppressWarnings("all")
public class Reflection {

    // Более быстрый способ для фетча значений
    private static final Map<ClassLoader, List<?>> cachedLoaders = new HashMap<>();

    // Так называемые 'var-args' которые необходимы для определения метода
    private static final Class<?>[] OLD_DATA = new Class<?>[] {
            String.class,
            byte[].class,
            int.class,
            int.class,
            ProtectionDomain.class,
            String.class
    };

    private static final Class<?>[] NEW_DATA = new Class<?>[] {
            ClassLoader.class,
            String.class,
            byte[].class,
            int.class,
            int.class,
            ProtectionDomain.class,
            String.class
    };

    // used for cache
    private static Method defineClassMethod;
    // used for cache
    private static int runtimeVersion = -1;

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            // Now unsafe can be accessed directly by method getUnsafe();
            ((Unsafe) field.get(null) ).
                    putObject(Reflection.class, Offset.of(Class.class, "classLoader"), null);
        } catch (Exception e) {
            throw new RuntimeException("");
        }
    }

    public static Unsafe lookup() {
        return getUnsafe();
    }

    public static int getRuntimeVersion() {
        if (Reflection.runtimeVersion != -1) return Reflection.runtimeVersion;

        String version = System.getProperty("java.version");
        if (version.startsWith("1.") ) version = version.substring(2, 3);
        else {
            int dot = version.indexOf(".");
            if (dot != -1) version = version.substring(0, dot);
        } 
        return Reflection.runtimeVersion = Integer.parseInt(version);
    }

    /**
     *
     * Fixes a problem with calling a method that
     * contains a native keyword via the Java Reflection API
     *
     * @param method The method to perform on
     */
    public static void unlockNative(Method method) {
        Reflection.setModifier(method, Modifier.PUBLIC);
    }

    private static void setModifier(Method method, int mod) {
        getUnsafe().putInt(method, Offset.of(Method.class, "modifiers"), mod);
    }

    private static Method getDefineClassMethod() throws NoSuchMethodException {
        if (Reflection.defineClassMethod != null) return Reflection.defineClassMethod;

        Method result;
        switch (Reflection.getRuntimeVersion() ) {
            case 8:
                result = ClassLoader.class.getDeclaredMethod("defineClass1", Reflection.OLD_DATA);
                break;
            case 16:
            case 17:
            default:
                result = ClassLoader.class.getDeclaredMethod("defineClass1", Reflection.NEW_DATA);
                break;
        }
        Reflection.unlockNative(result);

        return Reflection.defineClassMethod = result;
    }

    public static void defineClass(String name, byte[] data)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        switch (Reflection.getRuntimeVersion() ) {
            case 8:
                getDefineClassMethod().invoke(ClassLoader.getSystemClassLoader(),
                        name, data, 0, data.length, null, null);
                break;
            case 16:
            case 17:
            default:
                getDefineClassMethod().invoke(ClassLoader.getSystemClassLoader(),
                        ClassLoader.getSystemClassLoader(), name, data, 0, data.length, null, null);
                break;
        }
    }

    public static boolean isClassPresents(String className, ClassLoader loader) {
        List<?> fetched = Reflection.cachedLoaders.get(loader);
        if (fetched == null) fetched = Reflection.getClasses(loader);
        for (Object o : fetched) if (className.equals(((Class<?>) o).getName() ) ) return true;
        return false;
    }

    private static List<?> getClasses(ClassLoader loader) {
        List<?> result = (List<?>) getUnsafe().getObject(loader, Offset.of(ClassLoader.class, "classes") );
        Reflection.cachedLoaders.put(loader, result);
        return result;
    }

    public static void redefineClassLoader(Class<?> clazz, ClassLoader loader) {
        getUnsafe().putObject(clazz, Offset.of(Class.class, "classLoader"), loader);
    }
}
