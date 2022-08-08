package ru.aslcraft.runtimeclassloader.api.reflect;

import ru.aslcraft.runtimeclassloader.api.api.Reflection;

public final class ReflectionFactory {

	private ReflectionFactory() { }

	public static Reflection createReflection() {
		ReflectionFactory.preInitializeImpl(); // will be cached by Class.class (next init <= 20 nano)
		return ReflectionImpl.cached();
	}

	private static void preInitializeImpl() {
		try { Class.forName("ru.aslcraft.lang.reflect.ReflectionImpl"); } catch (final Exception ignored) { }
	}
}
