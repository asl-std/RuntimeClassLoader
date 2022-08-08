package ru.aslcraft;

import java.util.Arrays;

import ru.aslcraft.api.dependency.loader.api.Reflection;
import ru.aslcraft.api.dependency.loader.network.MavenClassLoader;
import ru.aslcraft.api.dependency.loader.network.MavenRepository;
import ru.aslcraft.api.dependency.loader.network.MavenURL;
import ru.aslcraft.api.dependency.loader.reflect.ReflectionFactory;

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
