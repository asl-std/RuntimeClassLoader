package net.tokyolancer;

import net.tokyolancer.lang.api.Reflection;
import net.tokyolancer.lang.network.MavenClassLoader;
import net.tokyolancer.lang.network.MavenRepository;
import net.tokyolancer.lang.network.MavenURL;
import net.tokyolancer.lang.reflect.ReflectionFactory;

import java.util.Arrays;

public class Main {

    public static void main(String[] args) throws Throwable {
        // define url
        // MavenURL url = new MavenURL(MavenRepository.Central, "commons-io","commons-io", "2.11.0");
        // create loader
        // MavenClassLoader loader = new MavenClassLoader(url);
        // call method
        // loader.loadClasses();

        Class<?> clazz = Class.forName("net.tokyolancer.lang.reflect.ReflectionImpl");


        Reflection reflection = ReflectionFactory.createReflection();
        System.out.println(Arrays.toString(reflection.getClass().getDeclaredFields() ) );
        System.out.println(Arrays.toString(reflection.getClass().getDeclaredMethods() ) );
    }
}
