package net.tokyolancer;

import net.tokyolancer.lang.exp.MavenClassLoader;
import net.tokyolancer.lang.network.MavenLibrary;
import net.tokyolancer.lang.network.MavenRepository;
import net.tokyolancer.lang.network.MavenURL;

public class Main {

    public static void main(String[] args) throws Throwable {
        MavenURL mavenURL = new MavenURL(MavenRepository.Central,"javax", "javaee-web-api", "8.0.1");
        MavenClassLoader classLoader = new MavenClassLoader(mavenURL, true);
        classLoader.loadClasses();

//        MavenClassLoader classLoader1 = new MavenClassLoader(MavenURL.fromMavenLibrary(MavenLibrary.ASM) );
//        classLoader1.loadClasses();
//
//        Class<?> clazz = Class.forName("org.apache.commons.io.ByteOrderMark");
//        System.out.println(clazz);

        Class<?> clazz = Class.forName("org.objectweb.asm.signature.SignatureWriter");
        System.out.println(clazz);
    }
}
