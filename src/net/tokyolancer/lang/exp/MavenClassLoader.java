package net.tokyolancer.lang.exp;

import net.tokyolancer.lang.network.MavenURL;
import net.tokyolancer.lang.reflect.Reflection;

import java.io.*;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 *
 */
public final class MavenClassLoader {

    private static final Method INJECTOR;

    static {
        try {
            INJECTOR = ClassLoader.class.getDeclaredMethod(
                            "defineClass1",
                            ClassLoader.class,
                            String.class,
                            byte[].class,
                            int.class,
                            int.class,
                            ProtectionDomain.class,
                            String.class
                    );
            Reflection.unlockNative(INJECTOR); // unlock first
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    // Confirms that the bytes were loaded correctly (the source of this data is the download link from the Maven repository)
    private final boolean isVerified;

    // Notifies that the list of loaded classes will be sorted
    private final boolean magicSort;

    private Map<String, byte[]> allEntries = new HashMap<>();
    private List<String> loadedAlready = new ArrayList<>();

    private byte[] origin;
    private int lastProblemsAmount = -1;

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

    private MavenClassLoader(byte[] data, boolean isVerified) {
        this(data, isVerified, true);
    }

    private MavenClassLoader(byte[] data, boolean isVerified, boolean magicSort) {
        this.origin = data;
        this.isVerified = isVerified;
        this.magicSort = magicSort;
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

    public void loadClasses() throws IOException {
        this.preLoadClasses();
        this.loadClasses0();

        // Удаляем прочие данные, потому что
        // повторная загрузка классов из одного
        // и того же экземпляра объекта - невозможна.
        this.loadedAlready = null;
        this.allEntries = null;
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

                this.allEntries.put(className, inStream.readAllBytes() );

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

//        for (Map.Entry<String, byte[]> entry : this.allEntries.entrySet() ) {
//            // Если класс уже был загружен, то смысла его подгружать - нет
//            if (this.loadedAlready.contains(entry.getKey() ) ) continue;
//            // Если класс не подгрузился по какой-то причине - обновляем счётчик
//            if (!loadClass0(entry.getKey(), entry.getValue() ) ) ++problems;
//        }

        // Более быстрый способ загрузки классов
        Iterator<Map.Entry<String, byte[]>> it = this.allEntries.entrySet().iterator();
        while (it.hasNext() ) {
            Map.Entry<String, byte[]> entry = it.next();
            if (this.loadedAlready.contains(entry.getKey() ) ) {
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
            // На самом деле не знаю, есть ли смысл какой лоадер использовать:
            // PlatformClassLoader или SystemClassLoader, а так-же стоит ли указывать
            // родительский лоадер. Так-же есть и другие аргументы функции, но они заменяемы
            // значениями NULL и в принципе можно быть спокойным.
            INJECTOR.invoke(ClassLoader.getSystemClassLoader(),
                    ClassLoader.getSystemClassLoader(), name, data, 0, data.length, null, null);
            // Запоминаем, что класс уже был подгружен
            this.loadedAlready.add(name);
        } catch (Exception e) {
//            System.out.printf("Failed to load class: %s\n", name);
            return false;
        }
        return true;
    }
}
