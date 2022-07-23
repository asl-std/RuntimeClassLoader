package net.tokyolancer.lang.reflect;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Reflection {

    public static final Unsafe UNSAFE;

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.trySetAccessible();
            UNSAFE = (Unsafe) field.get(null);
        } catch (Exception e) {
            throw new RuntimeException("");
        }
    }

    /**
     *
     * Fixes a problem with calling a method that
     * contains a native keyword via the Java Reflection API
     *
     * @param method The method to perform on
     */
    public static void unlockNative(Method method) {
        UNSAFE.putInt(method, Offset.int_MODIFIERS, Modifier.PUBLIC);
    }
}
