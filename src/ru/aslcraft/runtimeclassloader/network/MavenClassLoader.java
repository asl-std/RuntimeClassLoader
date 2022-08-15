package ru.aslcraft.runtimeclassloader.network;

import com.google.common.collect.ImmutableList;
import ru.aslcraft.runtimeclassloader.reflect.ReflectionFactory;
import ru.aslcraft.runtimeclassloader.util.FileUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 */
public final class MavenClassLoader {

	// Used only for debug
	private static final boolean isDebugging = true;

	// Notifies that the list of loaded classes will be sorted
	private final boolean magicSorting;

	private final List<Class<?>> loadedClasses = new ArrayList<>();

	private Map<String, byte[]> allEntries = new ConcurrentHashMap<>();

	private byte[] origin;
	private int lastProblemsAmount = -1;

	public MavenClassLoader(MavenLibrary lib) throws IOException {
		this(MavenURL.fromDependency(lib));

		ImmutableList<Dependency> dependencies = lib.getDependencies();

		if (dependencies.size() == 0) return;

		dependencies.forEach(dependency -> {
			try {
				if (isDebugging)
					System.out.println("Loading dependency " + dependency.groupId() + "." + dependency.artifactId());

				new MavenClassLoader(MavenURL.fromDependency(dependency)).loadClasses();
			} catch (IOException e) {
				if (isDebugging)
					System.out.println("Could't load dependency "
							+ dependency.groupId() + "." + dependency.artifactId()
							+ " for library " + lib.groupId() + "." + lib.artifactId());
			}
		});
	}

	public MavenClassLoader(MavenURL url) throws IOException {
		this(url, true);
	}

	public MavenClassLoader(MavenURL url, boolean magicSort) throws IOException {
		this(url.download(), magicSort);
	}

	private MavenClassLoader(byte[] data) {
		this(data, true);
	}

	private MavenClassLoader(byte[] data, boolean magicSort) {
		origin = data;
		magicSorting = magicSort;
	}

	public List<Class<?>> loadClasses() throws IOException {
		this.preLoadClasses();
		this.loadClasses0();

		// Удаляем прочие данные, потому что
		// повторная загрузка классов из одного
		// и того же экземпляра объекта - невозможна.
		allEntries = null;
		origin = null;

		return loadedClasses;
	}

	private void preLoadClasses() throws IOException {
		JarFile file = FileUtil.toJarFile(this.origin);

		FileUtil.performOnEntries(file, this::loadEntry);

		// Не забываем закрыть считывание файла
		file.close();

		if (magicSorting) {
			// Сортируем список по ключу (название класса) в зависимости с алфавитом.
			// На практике позволяет подгружать сразу намного больше классов, чем ежели без этого.
			allEntries = allEntries.entrySet().stream().sorted(Map.Entry.comparingByKey() )
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (k, v) -> k, LinkedHashMap::new) );
		}
	}

	private void loadEntry(ZipFile file, ZipEntry entry) {
		try {
			this.loadEntry0((JarFile) file, (JarEntry) entry);
		} catch (IOException ignored) { }
	}

	private void loadEntry0(JarFile file, JarEntry entry) throws IOException {
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

		// Считываем данные
		InputStream inStream = file.getInputStream(entry);

		allEntries.put(className, NetUtil.toByteArray(inStream) );

		// Не забываем закрывать поток
		inStream.close();
	}

	int loaded = 0;

	private void loadClasses0() {
		// Счётчик незагруженных классов
		int problems = 0;

		// Более быстрый способ загрузки классов
		Iterator<Map.Entry<String, byte[]>> it = this.allEntries.entrySet().iterator();

		if (MavenClassLoader.isDebugging && loaded == 0) {
			System.out.printf("Preparing to load %s classes!\n", this.allEntries.size() );
		}

		while (it.hasNext() ) {
			Map.Entry<String, byte[]> entry = it.next();

			// Now can be checked directly from classloader's class-list
			if (ReflectionFactory.createReflection().isClassPresents(entry.getKey(), ClassLoader.getSystemClassLoader() ) ) {
				it.remove();
				continue;
			}
			if (!loadClass0(entry.getKey(), entry.getValue() ) ) ++problems;
		}

		if (MavenClassLoader.isDebugging) {
			loaded += allEntries.size() - problems;
			System.out.printf("Loaded: %s / Not loaded: %s\n", loaded, allEntries.size() );
		}

		// Если есть незагруженные классы и количество прошлых незагруженных
		// классов (из-за рекурсии) не равняется текущему количеству (в ином случае рекурсия будет бесконечной).
		if (problems != 0 && lastProblemsAmount != problems) {
			lastProblemsAmount = problems;
			loadClasses0();
		}
	}

	private boolean loadClass0(String name, byte[] data) {
		try {
			Class<?> clazz = ReflectionFactory.createReflection().defineClass(name, data);
			// add to currently loaded class list
			loadedClasses.add(clazz);
		} catch (Exception e) {
			if (MavenClassLoader.isDebugging) {
				//System.out.printf("Failed to load class: %s\n", name);
				//e.printStackTrace();
			}
			return false;
		}
		return true;
	}
}
