package net.tokyolancer.lang.reflect;

import net.tokyolancer.lang.api.Reflection;

public final class ReflectionFactory {

    ReflectionFactory() { }

    public static Reflection createReflection() {
        ReflectionFactory.preInitializeImpl(); // will be cached by Class.class
        return ReflectionImpl.cached();
    }

    private static void preInitializeImpl() {
        try { Class.forName("net.tokyolancer.lang.reflect.ReflectionImpl"); } catch (Exception ignored) { }
    }
}
