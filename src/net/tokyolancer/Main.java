package net.tokyolancer;

import net.tokyolancer.lang.exp.MavenClassLoader;
import net.tokyolancer.lang.network.MavenLibrary;
import net.tokyolancer.lang.network.MavenURL;
import net.tokyolancer.lang.reflect.Offset;
import net.tokyolancer.lang.reflect.Reflection;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class Main {

    public static void main(String[] args) throws Throwable {
//        MavenURL mavenURL = new MavenURL(MavenRepository.Central,"javax", "javaee-web-api", "8.0.1");
//        MavenClassLoader classLoader = new MavenClassLoader(mavenURL, true);
//        classLoader.loadClasses();

        System.out.println(Reflection.isClassPresents("org.objectweb.asm.signature.SignatureWriter", Main.class.getClassLoader() ) );

        MavenClassLoader classLoader1 = new MavenClassLoader(MavenURL.fromMavenLibrary(MavenLibrary.ASM) );
        classLoader1.loadClasses();
//
//        Class<?> clazz = Class.forName("org.apache.commons.io.ByteOrderMark");
//        System.out.println(clazz);

//        Class<?> clazz = Class.forName("org.objectweb.asm.signature.SignatureWriter");

        System.out.println(Reflection.isClassPresents("org.objectweb.asm.signature.SignatureWriter", ClassLoader.getSystemClassLoader() ) );
    }

    private static class CustomLoader extends ClassLoader {

        public CustomLoader() {
            super(ClassLoader.getSystemClassLoader() );
        }
    }
}
