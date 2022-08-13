package ru.aslcraft.runtimeclassloader.network;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import ru.aslcraft.runtimeclassloader.reflect.ReflectionFactory;

/**
 *
 */
public final class MavenClassLoader {

	// Used only for debug
	private static final boolean isDebugging = true;

	// Confirms that the bytes were loaded correctly (the source of this data is the download link from the Maven repository)
	private final boolean isVerified;

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
		this(url.download(), true, magicSort);
	}

	@Deprecated
	public MavenClassLoader(byte[] data) {
		this(data, false);
	}

	private MavenClassLoader(byte[] data, boolean verified) {
		this(data, verified, true);
	}

	private MavenClassLoader(byte[] data, boolean verified, boolean magicSort) {
		origin = data;
		isVerified = verified;
		magicSorting = magicSort;
	}

	public List<Class<?>> loadClasses() throws IOException {
		this.preLoadClasses();
		this.loadClasses0();

		// Удаляем прочие данные, потому что
		// повторная загрузка классов из одного
		// и того же экземпляра объекта - невозможна.
		allEntries = null;

		return loadedClasses;
	}

	private JarFile createJar() throws IOException {
		// Создаёт временный файл в директории /TEMP/ текущей ОС.
		// Имеет такой вид: mcl{набор_цифр}.tmp
		File tempFile = File.createTempFile("mcl", null, null);
		tempFile.deleteOnExit();

		FileOutputStream fos = new FileOutputStream(tempFile);
		fos.write(origin);
		fos.flush();
		fos.close();

		// Обрезаем поинтер чтобы GC при
		// следующей очистке удалил референс из памяти
		origin = null;

		JarFile result = null;

		// Если байты не были ранее верифицированы,
		// то мы создаём инстансу под блоком try-catch
		if (!isVerified) {
			try {
				result = new JarFile(tempFile);
			} catch (Exception ignored) { }
		} else result = new JarFile(tempFile);
		return result;
	}

	private void preLoadClasses() throws IOException {
		JarFile file = this.createJar();

		if (file != null) {
			Enumeration<JarEntry> entries = file.entries();

			while (entries.hasMoreElements() ) {
				JarEntry entry = entries.nextElement();

				// Если это директория - скипаем
				if (entry.isDirectory() ) continue;

				final String name = entry.getName();

				// Скипаем всё что не относится к файлу типа '.class'
				if (!name.endsWith(".class")
						|| name.endsWith("package-info.class")
						|| name.endsWith("module-info.class") ) continue;

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
			// Не забываем закрыть считывание файла
			file.close();
		}
		if (magicSorting) {
			// Сортируем список по ключу (название класса) в зависимости с алфавитом.
			// На практике позволяет подгружать сразу намного больше классов, чем ежели без этого.
			allEntries = allEntries.entrySet().stream().sorted(Map.Entry.comparingByKey() )
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (k, v) -> k, LinkedHashMap::new) );
		}
	}

	int loaded = 0;

	private void loadClasses0() {
		// Счётчик незагруженных классов
		int problems = 0;

		// Более быстрый способ загрузки классов
		Iterator<Map.Entry<String, byte[]>> it = allEntries.entrySet().iterator();
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
			System.out.printf("Loaded %s / %s\n", loaded, allEntries.size());
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
