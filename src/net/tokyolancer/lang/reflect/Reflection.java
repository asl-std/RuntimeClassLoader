package net.tokyolancer.lang.reflect;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;

public class Reflection {

    public static final Unsafe UNSAFE;

    // Так называемые 'var-args' которые необходимы для определения метода
    private static Class<?>[] OLD_DATA = new Class<?>[] {
            String.class,
            byte[].class,
            int.class,
            int.class,
            ProtectionDomain.class,
            String.class
    };

    private static Class<?>[] NEW_DATA = new Class<?>[] {
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
            UNSAFE = (Unsafe) field.get(null);
        } catch (Exception e) {
            throw new RuntimeException("");
        }
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
        UNSAFE.putInt(method, Offset.get_int_MODIFIERS(), Modifier.PUBLIC);
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

    public static void defineClass(String name, byte[] data) throws NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {
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
}
