package ru.aslcraft.runtimeclassloader.unsafe;

public class UnsafeFactory {

    private UnsafeFactory() { }

    public static UnsafeImpl createUnsafe() {
        UnsafeFactory.preInitializeImpl(); // will be cached by Class.class (next init <= 20 nano)
        return UnsafeImpl.cached();
    }

    private static void preInitializeImpl() {
        try { Class.forName("ru.aslcraft.lang.unsafe.UnsafeImpl"); } catch (final Exception ignored) { }
    }
}
