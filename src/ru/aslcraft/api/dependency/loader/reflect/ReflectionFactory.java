package ru.aslcraft.api.dependency.loader.reflect;

import ru.aslcraft.api.dependency.loader.api.Reflection;

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
