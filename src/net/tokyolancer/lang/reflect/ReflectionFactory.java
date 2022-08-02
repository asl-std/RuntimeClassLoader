package net.tokyolancer.lang.reflect;

import net.tokyolancer.lang.api.Reflection;

public final class ReflectionFactory {

    private ReflectionFactory() { }

    public static Reflection createReflection() {
        ReflectionFactory.preInitializeImpl(); // will be cached by Class.class (next init <= 20 nano)
        return ReflectionImpl.cached();
    }

    private static void preInitializeImpl() {
         try { Class.forName("net.tokyolancer.lang.reflect.ReflectionImpl"); } catch (Exception ignored) { }
    }
}
