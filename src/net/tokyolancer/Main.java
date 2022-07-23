package net.tokyolancer;

import net.tokyolancer.lang.exp.MavenClassLoader;
import net.tokyolancer.lang.network.MavenLibrary;
import net.tokyolancer.lang.network.MavenRepository;
import net.tokyolancer.lang.network.MavenURL;

public class Main {

    public static void main(String[] args) throws Throwable {
        MavenURL mavenURL = new MavenURL(MavenRepository.Central,
                "commons-io", "commons-io", "2.11.0");
        MavenClassLoader classLoader = new MavenClassLoader(mavenURL);
        classLoader.loadClasses();

        classLoader = new MavenClassLoader(MavenURL.fromMavenLibrary(MavenLibrary.OracleJDBC) );
        classLoader.loadClasses();

        Class<?> clazz = Class.forName("org.apache.commons.io.ByteOrderMark");
        System.out.println(clazz);
    }
}
