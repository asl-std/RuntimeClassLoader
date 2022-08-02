package net.tokyolancer.lang.api;

import sun.misc.Unsafe;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public interface Reflection {

    Unsafe lookup();

    /**
     *
     * Fixes a problem with calling a method that
     * contains a native keyword via the Java Reflection API
     *
     * @param method The method to perform on
     */
    void unlockNative(Method method);

    /**
     *
     * Allows you to initialize a class with the correct name
     * and automatically load it into runtime in SystemClassLoader loader
     *
     * @param name Correct class name
     * @param data Correct class byte data
     * @throws NoSuchMethodException This error cannot be thrown, but it will have to be handled (because I am pussy)
     * @throws InvocationTargetException If the data or name is incorrect (check docs before using this method)
     * @throws IllegalAccessException This error cannot be thrown, but it will have to be handled (because I am pussy)
     */
    void defineClass(String name, byte[] data)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException;

    /**
     *
     * Checks whether the class is loaded in the specified class loader
     *
     * @param className Class name to check
     * @param loader Exact class loader
     * @return If loaded - true, otherwise - false
     */
    boolean isClassPresents(String className, ClassLoader loader);

    /**
     *
     * Allows you to change the class loader of the specified class to any other loader
     *
     * @param clazz Exact class
     * @param loader Exact class loader
     */
    void redefineClassLoader(Class<?> clazz, ClassLoader loader);

    /**
     * Allows you to call invoke on an instance of a method with <b>all privileges</b>
     *
     * @param method The method to be executed
     * @param o The object the underlying method is invoked from
     * @param args The arguments used for the method call
     * @return The result of dispatching the method represented by this object on obj with parameters args
     * @throws InvocationTargetException If the underlying method throws an exception
     * @throws IllegalAccessException If this Method object is enforcing Java language access control and the underlying method is inaccessible
     */
    Object godInvoke(Method method, Object o, Object... args)
            throws InvocationTargetException, IllegalAccessException;
}
