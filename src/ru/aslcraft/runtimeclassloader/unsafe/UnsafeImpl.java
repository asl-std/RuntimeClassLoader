package ru.aslcraft.runtimeclassloader.unsafe;

import ru.aslcraft.runtimeclassloader.api.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class UnsafeImpl implements Unsafe {

    // used for cache
    private static final Class<?> unsafeClazz;
    // used for cache
    private static final Object unsafeInstance;
    // used for cache
    private static final Method putObjectMethod;
    // used for cache
    private static final Method getObjectMethod;
    // used for cache
    private static final Method putIntegerMethod;

    // must be defined by constructor
    private static UnsafeImpl root = null;

    static {
        try {
            unsafeClazz = Class.forName("sun.misc.Unsafe");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            Field field = unsafeClazz.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafeInstance = field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        try {
            putObjectMethod = unsafeClazz.getDeclaredMethod("putObject", Object.class, long.class, Object.class);
            getObjectMethod = unsafeClazz.getDeclaredMethod("getObject", Object.class, long.class);
            putIntegerMethod = unsafeClazz.getDeclaredMethod("putInt", Object.class, long.class, int.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    UnsafeImpl() {
        UnsafeImpl.root = this; // caching it-self
    }

    static UnsafeImpl cached() {
        if (UnsafeImpl.root == null)
            return new UnsafeImpl();
        return UnsafeImpl.root;
    }

    @Override
    public void putObject(Object o, long offset, Object x) {
        try {
            UnsafeImpl.putObjectMethod.invoke(UnsafeImpl.unsafeInstance, o, offset, x);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void putInt(Object o, long offset, int x) {
        try {
            UnsafeImpl.putIntegerMethod.invoke(UnsafeImpl.unsafeInstance, o, offset, x);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object getObject(Object o, long offset) {
        try {
            return UnsafeImpl.getObjectMethod.invoke(UnsafeImpl.unsafeInstance, o, offset);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
