package ru.aslcraft;

import java.util.Arrays;

import ru.aslcraft.runtimeclassloader.api.api.Reflection;
import ru.aslcraft.runtimeclassloader.api.network.MavenClassLoader;
import ru.aslcraft.runtimeclassloader.api.network.MavenRepository;
import ru.aslcraft.runtimeclassloader.api.network.MavenURL;
import ru.aslcraft.runtimeclassloader.api.reflect.ReflectionFactory;

public class Main {

	public static void main(String[] args) throws Throwable {
		final MavenURL url = new MavenURL(MavenRepository.Central, "commons-io", "commons-io", "2.11.0");
		final MavenClassLoader loader = new MavenClassLoader(url);
		System.out.println(loader.loadClasses());

		final Reflection reflection = ReflectionFactory.createReflection();
		System.out.println(Arrays.toString(reflection.getClass().getDeclaredFields()));
		System.out.println(Arrays.toString(reflection.getClass().getDeclaredMethods()));
	}
}
