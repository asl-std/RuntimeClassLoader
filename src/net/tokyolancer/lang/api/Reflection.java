package net.tokyolancer.lang.api;

import net.tokyolancer.lang.reflect.Cached;
import sun.misc.Unsafe;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class Reflection {

    public abstract Unsafe lookup();

    public abstract void unlockNative(Method method);

    public abstract void defineClass(String name, byte[] data)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException;

    public abstract @Cached boolean isClassPresents(String className, ClassLoader loader);

    public abstract void redefineClassLoader(Class<?> clazz, ClassLoader loader);

    public abstract Object godInvoke(Method method, Object o, Object... args)
            throws InvocationTargetException, IllegalAccessException;
}
