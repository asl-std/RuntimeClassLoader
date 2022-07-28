package net.tokyolancer;

import net.tokyolancer.lang.exp.MavenRTClassLoader;
import net.tokyolancer.lang.network.MavenRepository;
import net.tokyolancer.lang.network.MavenURL;
import net.tokyolancer.lang.reflect.Reflection;

import java.lang.reflect.Method;

public class Main {

    public static void test() { }

    public static void main(String[] args) throws Throwable {
        MavenURL mavenURL = new MavenURL(MavenRepository.Central,"org.postgresql", "postgresql", "42.4.0");
        MavenRTClassLoader classLoader = new MavenRTClassLoader(mavenURL);
        classLoader.loadClasses();

//        Reflection.directInvoke(method, null, MavenLibrary.ASM);

//        System.out.println(Reflection.lookup().getObject(method, 80L) instanceof MethodAccessor);

//        MavenURL mavenURL = MavenURL.fromMavenLibrary(MavenLibrary.ASM);
//        MavenRTClassLoader classLoader1 = new MavenRTClassLoader(mavenURL);
    }
}
