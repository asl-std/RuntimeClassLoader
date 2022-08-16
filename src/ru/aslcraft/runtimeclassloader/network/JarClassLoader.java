package ru.aslcraft.runtimeclassloader.network;

import com.google.common.collect.ImmutableList;
import ru.aslcraft.runtimeclassloader.reflect.ReflectionFactory;
import ru.aslcraft.runtimeclassloader.util.FileUtil;
import ru.aslcraft.runtimeclassloader.util.NetUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 */
public final class JarClassLoader {

	// Used only for debug
	private static final boolean isDebugging = true;

	private final Map<String, byte[]> allEntries = new ConcurrentHashMap<>();
	private final List<Class<?>> loadedClasses = new ArrayList<>();

	private final JarFile jar;

	// For removal
	@Deprecated
	public JarClassLoader(MavenLibrary lib) throws IOException {
		this(MavenURL.fromDependency(lib));

		ImmutableList<Dependency> dependencies = lib.getDependencies();

		if (dependencies.size() == 0) return;

		dependencies.forEach(dependency -> {
			try {
				if (isDebugging)
					System.out.println("Loading dependency " + dependency.groupId() + "." + dependency.artifactId());

				new JarClassLoader(MavenURL.fromDependency(dependency)).loadClasses();
			} catch (IOException e) {
				if (isDebugging)
					System.out.println("Could't load dependency "
							+ dependency.groupId() + "." + dependency.artifactId()
							+ " for library " + lib.groupId() + "." + lib.artifactId());
			}
		});
	}

	// For removal
	@Deprecated
	public JarClassLoader(MavenURL url) throws IOException {
		this(url.download() );
	}

	/**
	 *
	 * Constructor is deprecated due to unsafe initializing of .jar file.
	 *
	 * @param data Jar file contents
	 * @throws IOException If some of IO operations went wrong
	 */
	@Deprecated
	public JarClassLoader(byte[] data) throws IOException {
		this(FileUtil.toJarFile(data) );
	}

	public JarClassLoader(JarFile jar) {
		this.jar = jar;
	}

	public List<Class<?>> loadClasses() throws IOException {
		this.preLoadClasses();
		this.loadClasses0();
		return loadedClasses;
	}


	// === Class Loading From Jar Entry === //


	private void preLoadClasses() throws IOException {
		FileUtil.performOnEntries(this.jar, this::loadEntry);
		// Не забываем закрыть считывание файла
		this.jar.close();
	}

	private void loadEntry(ZipFile file, ZipEntry entry) {
		// Если это директория - скипаем
		if (entry.isDirectory() ) return;

		final String name = entry.getName();

		// Скипаем всё что не относится к файлу типа '.class'
		if (!name.endsWith(".class")
				|| name.endsWith("package-info.class")
				|| name.endsWith("module-info.class") ) return;

		// Так как мы вытаскиваем название пути из
		// псевдо-архива, то путь будет иметь разделитель - '/'
		final String className = name.replace(".class", "")
				.replace("/", ".");

		try {
			// Считываем данные
			InputStream inStream = file.getInputStream(entry);

			// Запоминаем данные - класс : байты класса
			this.allEntries.put(className, NetUtil.toByteArray(inStream) );

			// Не забываем закрывать поток
			inStream.close();
		} catch (IOException e) {
			throw new RuntimeException(String.format("Failed to get bytes from entry with name %s", name), e);
		}
	}


	// === Class Loading Into Runtime === //


	int loaded = 0;
	int lastProblemsAmount = -1;

	private void loadClasses0() {
		// Счётчик незагруженных классов
		int problems = 0;

		// Более быстрый способ загрузки классов
		Iterator<Map.Entry<String, byte[]>> it = this.allEntries.entrySet().iterator();

		if (JarClassLoader.isDebugging && loaded == 0) {
			System.out.printf("Preparing to load %s classes!\n", this.allEntries.size() );
		}

		while (it.hasNext() ) {
			Map.Entry<String, byte[]> entry = it.next();

			// Now can be checked directly from classloader's class-list
			if (ReflectionFactory.createReflection().isClassPresents(entry.getKey(), ClassLoader.getSystemClassLoader() ) ) {
				it.remove();
				continue;
			}
			if (!loadClass(entry.getKey(), entry.getValue() ) ) ++problems;
		}

		if (JarClassLoader.isDebugging) {
			loaded += allEntries.size() - problems;
			System.out.printf("Loaded: %s / Not loaded: %s\n", loaded, allEntries.size() );
		}

		// Рекурсивно подгружаем оставшиеся классы, если они не загрузились по какой-то из причин
		if (problems != 0 && lastProblemsAmount != problems) {
			lastProblemsAmount = problems;
			loadClasses0();
		}
	}

	private boolean loadClass(String name, byte[] data) {
		Class<?> clazz = null;

		try {
			// add to currently loaded class list
			loadedClasses.add(clazz = ReflectionFactory.createReflection().defineClass(name, data) );
		} catch (Exception e) {
			if (JarClassLoader.isDebugging) {
				System.out.printf("Failed to load class: %s\n", name);
				e.printStackTrace();
			}
		}

		return clazz != null;
	}
}
