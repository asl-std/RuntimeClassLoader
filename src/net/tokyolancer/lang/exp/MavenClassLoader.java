package net.tokyolancer.lang.exp;

import net.tokyolancer.lang.network.MavenURL;
import net.tokyolancer.lang.reflect.Reflection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class MavenClassLoader {

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

    private final List<String> loadedAlready = new ArrayList<>();

    private final byte[] origin;
    private final boolean isVerified;

    private Map<String, byte[]> allEntries = new HashMap<>();

//    private JarInputStream lastInStream;
    private JarFile lastJarFile;
    private int lastProblemsAmount = -1;

    public MavenClassLoader(MavenURL url) throws IOException {
        this(url.download(), true);
    }

    @Deprecated
    public MavenClassLoader(byte[] data) {
        this(data, false);
    }

    private MavenClassLoader(byte[] data, boolean isVerified) {
        this.origin = data;
        this.isVerified = isVerified;
    }

//    public JarInputStream jarStream() throws IOException {
//        if (this.lastInStream != null) return this.lastInStream;
//        return this.lastInStream = new JarInputStream(new ByteArrayInputStream(this.origin) );
//    }

    public JarFile createJar() throws IOException {
        if (this.lastJarFile != null) return this.lastJarFile;

        // Создаёт файл в директории /TEMP/ текущей ОС.
        // Имеет такой вид: cls{набор_цифр}.tmp
        File tempFile = File.createTempFile("cls", null, null);
        FileOutputStream fos = new FileOutputStream(tempFile);
        fos.write(this.origin);
        fos.flush();
        fos.close();

        return this.lastJarFile = new JarFile(tempFile);
    }

    public void loadClasses() throws IOException {
        this.preLoadClasses();
        this.loadClasses0();
    }

    private void preLoadClasses() throws IOException {
        JarFile file = this.createJar();

        if (file != null) {
            Enumeration<JarEntry> entries = file.entries();

            while (entries.hasMoreElements() ) {
                JarEntry entry = entries.nextElement();

                // if entry is directory - skip
                if (entry.isDirectory() ) continue;

                final String name = entry.getName();

                // if entry is file, but it is not a .class file
                if (!name.endsWith(".class") || name.endsWith("package-info.class") ) continue;

                final String className = name.replace(".class", "").replace("/", ".");

                InputStream stream = file.getInputStream(entry);

                this.allEntries.put(className, stream.readAllBytes() );
            }
        }
        // sort
        this.allEntries = this.allEntries.entrySet().stream().sorted(Map.Entry.comparingByKey() )
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new) );
    }

    private void loadClasses0() {
        int problems = 0;
        String tmp;
        for (Map.Entry<String, byte[]> entry : this.allEntries.entrySet() ) {
             if (this.loadedAlready.contains(entry.getKey() ) ) continue;
             if ((tmp = loadClass0(entry.getKey(), entry.getValue() ) ) != null) ++problems;
        }
        System.out.printf("Loaded %s / %s\n", this.allEntries.size() - problems, this.allEntries.size() );

        if (problems != 0 && lastProblemsAmount != problems) {
            this.lastProblemsAmount = problems;
            loadClasses0();
        }
    }

    private String loadClass0(String name, byte[] data) {
        String neededClassName = null;
        try {
            INJECTOR.invoke(ClassLoader.getSystemClassLoader(),
                    ClassLoader.getSystemClassLoader(), name, data, 0, data.length, null, null);
             this.loadedAlready.add(name);
        } catch (Exception e) {
            neededClassName = e.getCause().toString().split(":")[1].trim();
            // System.out.printf("Failed to load class with name %s due to unloaded class %s: %n", name, neededClassName);
        }
        return neededClassName;
    }

//    public void loadClasses0() throws IOException {
//        JarFile file = this.createJar();
//        int loaded = 0;
//        int confirmed = 0;
//        if (file != null) {
//            Enumeration<JarEntry> entries = file.entries();
//
//            while (entries.hasMoreElements() ) {
//                ++loaded; // counter
//
//                JarEntry entry = entries.nextElement();
//
//                if (entry.isDirectory() ) continue;
//
//                final String name = entry.getName();
//
//                if (!name.endsWith(".class") || name.endsWith("package-info.class") ) continue;
//
//                final String className = name.replace(".class", "").replace("/", ".");
//
//                InputStream stream = file.getInputStream(entry);
//                byte[] data = stream.readAllBytes();
//
//                try {
//                    INJECTOR.invoke(ClassLoader.getSystemClassLoader(),
//                            ClassLoader.getSystemClassLoader(), className, data, 0, data.length, null, null);
//                    ++confirmed; // if loaded
//                    this.loadedAlready.add(className); // if loaded
////                    System.out.printf("Loaded: %s\n", className);
//                } catch (Exception e) {
//                    final String neededClassName = e.getCause().toString().split(":")[1].trim();
//                    System.out.printf("Failed to load class with name %s due to unloaded class %s: %n", className, neededClassName);
//                }
//            }
//        }
////        if (loaded != confirmed) loadClasses0();
//        System.out.printf("Loaded %s / %s%n", confirmed, loaded);
//    }
}
