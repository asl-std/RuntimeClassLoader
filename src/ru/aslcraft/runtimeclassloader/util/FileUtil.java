package ru.aslcraft.runtimeclassloader.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileUtil {

    @SuppressWarnings("unchecked")
    public static List<? extends JarEntry> filterEntries(JarFile file, BiFunction<ZipFile, ZipEntry, Boolean> biFunction) {
        return (List<? extends JarEntry>) FileUtil.filterEntries((ZipFile) file, biFunction);
    }

    // testing
    public static List<? extends ZipEntry> filterEntries(ZipFile file, BiFunction<ZipFile, ZipEntry, Boolean> biFunction) {
        List<ZipEntry> result = new LinkedList<>();
        Enumeration<? extends ZipEntry> entries = file.entries();

        while (entries.hasMoreElements() ) {
            ZipEntry entry = entries.nextElement();
            if (biFunction.apply(file, entry) )
                result.add(entry);
        }

        return result;
    }

    public static JarFile toJarFile(byte[] data) throws IOException {
        return new JarFile(FileUtil.toFile(data) );
    }

    public static File toFile(byte[] data) throws IOException {
        File tempFile = File.createTempFile("murl", null, null);
        tempFile.deleteOnExit();

        FileOutputStream fos = new FileOutputStream(tempFile);
        fos.write(data);
        fos.flush();
        fos.close();

        return tempFile;
    }
}
