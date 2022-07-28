package net.tokyolancer.lang.exp;

import net.tokyolancer.lang.network.MavenURL;
import net.tokyolancer.lang.network.NetUtil;
import net.tokyolancer.lang.reflect.Reflection;

import java.io.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 *
 */
public final class MavenRTClassLoader {

    // Confirms that the bytes were loaded correctly (the source of this data is the download link from the Maven repository)
    private final boolean isVerified;

    // Notifies that the list of loaded classes will be sorted
    private final boolean magicSort;

    private Map<String, byte[]> allEntries = new HashMap<>();
    // private List<?> loadedAlready = new ArrayList<>();

    private byte[] origin;
    private int lastProblemsAmount = -1;

    public MavenRTClassLoader(MavenURL url) throws IOException {
        this(url, true);
    }

    public MavenRTClassLoader(MavenURL url, boolean magicSort) throws IOException {
        this(url.download(), true, magicSort);
    }

    @Deprecated
    public MavenRTClassLoader(byte[] data) {
        this(data, false);
    }

    private MavenRTClassLoader(byte[] data, boolean isVerified) {
        this(data, isVerified, true);
    }

    private MavenRTClassLoader(byte[] data, boolean isVerified, boolean magicSort) {
        this.origin = data;
        this.isVerified = isVerified;
        this.magicSort = magicSort;
    }

    public void loadClasses() throws IOException {
        this.preLoadClasses();
        this.loadClasses0();

        // Удаляем прочие данные, потому что
        // повторная загрузка классов из одного
        // и того же экземпляра объекта - невозможна.
        this.allEntries = null;
    }

    private JarFile createJar() throws IOException {
        // Создаёт временный файл в директории /TEMP/ текущей ОС.
        // Имеет такой вид: mcl{набор_цифр}.tmp
        File tempFile = File.createTempFile("mcl", null, null);
        tempFile.deleteOnExit();

        FileOutputStream fos = new FileOutputStream(tempFile);
        fos.write(this.origin);
        fos.flush();
        fos.close();

        // Обрезаем поинтер чтобы GC при
        // следующей очистке удалил референс из памяти
        this.origin = null;

        JarFile result = null;

        // Если байты не были ранее верифицированы,
        // то мы создаём инстансу под блоком try-catch
        if (!this.isVerified) {
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

                this.allEntries.put(className, NetUtil.toByteArray(inStream) );

                // Не забываем закрывать поток
                inStream.close();
            }
            // Не забываем закрыть считывание файла
            file.close();
        }
        if (this.magicSort)
            // Сортируем список по ключу (название класса) в зависимости с алфавитом.
            // На практике позволяет подгружать сразу намного больше классов, чем ежели без этого.
            this.allEntries = this.allEntries.entrySet().stream().sorted(Map.Entry.comparingByKey() )
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new) );
    }

    int loaded = 0;

    private void loadClasses0() {
        // Счётчик незагруженных классов
        int problems = 0;

        // Более быстрый способ загрузки классов
        Iterator<Map.Entry<String, byte[]>> it = this.allEntries.entrySet().iterator();
        while (it.hasNext() ) {
            Map.Entry<String, byte[]> entry = it.next();
            // Now can be checked directly from classloader's class-list
            if (Reflection.isClassPresents(entry.getKey(), ClassLoader.getSystemClassLoader() ) ) {
                it.remove();
                continue;
            }
            if (!loadClass0(entry.getKey(), entry.getValue() ) ) ++problems;
        }

        loaded += this.allEntries.size() - problems;

        System.out.printf("Loaded %s / %s\n", loaded, this.allEntries.size() );

        // Если есть незагруженные классы и количество прошлых незагруженных
        // классов (из-за рекурсии) не равняется текущему количеству (в ином случае рекурсия будет бесконечной).
        if (problems != 0 && lastProblemsAmount != problems) {
            this.lastProblemsAmount = problems;
            loadClasses0();
        }
    }

    private boolean loadClass0(String name, byte[] data) {
        try {
            Reflection.defineClass(name, data);
        } catch (Exception ignored) {
            // ignored.printStackTrace();
            // System.out.printf("Failed to load class: %s\n", name);
            return false;
        }
        return true;
    }
}
