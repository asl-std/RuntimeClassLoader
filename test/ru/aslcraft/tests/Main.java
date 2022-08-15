package ru.aslcraft.tests;

import org.apache.maven.model.Dependency;
import ru.aslcraft.runtimeclassloader.network.CustomClassLoader;
import ru.aslcraft.runtimeclassloader.network.MavenClassLoader;
import ru.aslcraft.runtimeclassloader.network.MavenLibrary;
import ru.aslcraft.runtimeclassloader.network.MavenURL;

public class Main {

	public static void main(String[] args) throws Throwable {
		/*for (final MavenLibrary lib : MavenLibrary.values()) {
			final MavenClassLoader loader = new MavenClassLoader(MavenURL.fromMavenLibrary(lib), true);

			System.out.println("Successfully loaded " + lib.groupId() + "." + lib.artifactId());

			System.out.println(loader.loadClasses());
		}*/



//		final MavenLibrary lib = MavenLibrary.MYSQL_CONNECTOR;
//
//		new CustomClassLoader(lib).resolveDependencies();

		final MavenClassLoader loader = new MavenClassLoader(MavenURL.fromDependency(MavenLibrary.ASM));
		loader.loadClasses();

//
//		System.out.println(lib.getDependencies().size() );
//
//		final MavenClassLoader loader = new MavenClassLoader(lib);
//
//		System.out.println("Successfully loaded " + lib.groupId() + "." + lib.artifactId());
//
//		System.out.println(loader.loadClasses());

		/*final Reflection reflection = ReflectionFactory.createReflection();
		System.out.println(Arrays.toString(reflection.getClass().getDeclaredFields()));
		System.out.println(Arrays.toString(reflection.getClass().getDeclaredMethods()));*/
	}

	public static void bumpPomToDependency() {

	}
}
