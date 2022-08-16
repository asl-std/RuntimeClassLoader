package ru.aslcraft.runtimeclassloader.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.function.BiConsumer;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class FileUtil {

    private FileUtil() { }

    public static void performOnEntries(JarFile file, BiConsumer<ZipFile, ZipEntry> biConsumer) {
        FileUtil.performOnEntries((ZipFile) file, biConsumer);
    }

    public static void performOnEntries(ZipFile file, BiConsumer<ZipFile, ZipEntry> biConsumer) {
        Enumeration<? extends ZipEntry> entries = file.entries();

        while (entries.hasMoreElements() )
            biConsumer.accept(file, entries.nextElement() );
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
