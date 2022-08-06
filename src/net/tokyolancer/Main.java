package net.tokyolancer;

import net.tokyolancer.lang.api.Predicted;
import net.tokyolancer.lang.api.Reflection;
import net.tokyolancer.lang.async.Vavilon;
import net.tokyolancer.lang.network.MavenClassLoader;
import net.tokyolancer.lang.network.MavenRepository;
import net.tokyolancer.lang.network.MavenURL;
import net.tokyolancer.lang.reflect.ReflectionFactory;

import java.io.File;
import java.util.Arrays;
import java.util.jar.JarFile;

public class Main {

    public static void main(String[] args) throws Throwable {
        MavenURL url = new MavenURL(MavenRepository.Central, "commons-io", "commons-io", "2.11.0");
        MavenClassLoader loader = new MavenClassLoader(url);
        System.out.println(loader.loadClasses() );

        Reflection reflection = ReflectionFactory.createReflection();
        System.out.println(Arrays.toString(reflection.getClass().getDeclaredFields() ) );
        System.out.println(Arrays.toString(reflection.getClass().getDeclaredMethods() ) );
    }
}
