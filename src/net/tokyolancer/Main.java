package net.tokyolancer;

import net.tokyolancer.lang.api.Predicted;
import net.tokyolancer.lang.api.Reflection;
import net.tokyolancer.lang.async.Vavilon;
import net.tokyolancer.lang.network.MavenClassLoader;
import net.tokyolancer.lang.network.MavenRepository;
import net.tokyolancer.lang.network.MavenURL;
import net.tokyolancer.lang.reflect.ReflectionFactory;

import java.util.Arrays;

public class Main {

    public static void main(String[] args) throws Throwable {
        Predicted<Boolean> predicted = Vavilon.Gates.openAsync(Main::test).await();
        Predicted<Boolean> predicted2 = Vavilon.Gates.openAsync(Main::test).await();

        Reflection reflection = ReflectionFactory.createReflection();
        System.out.println(Arrays.toString(reflection.getClass().getDeclaredFields() ) );
        System.out.println(Arrays.toString(reflection.getClass().getDeclaredMethods() ) );
    }

    static void test() {
        try {
            Class.forName("net.tokyolancer.lang.reflect.ReflectionImpl");
        } catch (ClassNotFoundException ignored) { }
        System.out.println("Called!");
    }
}
