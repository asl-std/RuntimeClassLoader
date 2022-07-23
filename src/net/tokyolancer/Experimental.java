package net.tokyolancer;

import net.tokyolancer.lang.network.MavenLibrary;
import net.tokyolancer.lang.network.MavenRepository;
import net.tokyolancer.lang.network.MavenURL;
import net.tokyolancer.lang.reflect.Reflection;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.ProtectionDomain;

public class Experimental {

    public static void main(String[] args) throws Throwable {
        // TODO: укажи абсолютный путь до файла (так удобнее будет)
        // TODO: именно нужен файл с раширением .class !!!
        // TODO: остальное робить не будет
        Path path = Paths.get("C:\\Users\\xaski\\Desktop\\LeetCode\\test\\Calculator.class");
        // простенький рид файла через путь
        byte[] data = Files.readAllBytes(path);

        Method loadClassMethod = ClassLoader.class
                .getDeclaredMethod(
                        "defineClass1",
                        ClassLoader.class,
                        String.class,
                        byte[].class,
                        int.class,
                        int.class,
                        ProtectionDomain.class,
                        String.class
                        );
        Reflection.unlockNative(loadClassMethod);

//        test1(data);
//        test2("net.tokyolancer.Calculator", data);

        System.out.println(Class.forName("net.tokyolancer.Calculator"));

//        loadClassMethod.invoke(ClassLoader.getSystemClassLoader(), null,
//                "net.tokyolancer.Calculator", data, 0, data.length, null, null);
//
//        System.out.println(Class.forName("net.tokyolancer.Calculator") );
    }

    public static void test1(byte[] data) throws IllegalAccessException {
        // Здесь первый способ (самый лёгкий из всех возможных).
        // Является более безопасным, потому что это паблик API.
        // И не нужно выворачиваться с какими-то там рефлекторами.
        // Вызвал метод, и жаба сама подгрузит класс в рантайм.
        //
        // В общем вроде-как доступно с JAVA SE 9
        // Поэтому проблем не должно быть
        //
        // P.S. Может ударить по голове ошибкой Illegal Access
        MethodHandles.lookup().defineClass(data);
    }

    public static void test2(String className, byte[] data) throws NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {
        // Здесь второй способ (исключительно для мазохистов:D).
        // Не очень безопасный из-за того, что оффсет нужного
        // поля может поменяться в следующей версии жабы.
        // Однако оффсет сменится в том случае, если поменяется java.lang.reflect.Method.java
        Method method = ClassLoader.class
                .getDeclaredMethod(
                        "defineClass1",
                        ClassLoader.class,
                        String.class,
                        byte[].class,
                        int.class,
                        int.class,
                        ProtectionDomain.class,
                        String.class
                );
        // Специальный анлок для нативных классов
        Reflection.unlockNative(method);
        // Берём дефолтный системный класс лоадер и ебашим
        method.invoke(ClassLoader.getSystemClassLoader(),
                ClassLoader.getSystemClassLoader(), className, data, 0, data.length, null, null);
    }
}
