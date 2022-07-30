package net.tokyolancer;

import net.tokyolancer.lang.api.Reflection;
import net.tokyolancer.lang.reflect.ReflectionFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) throws Throwable {
        Reflection reflection = ReflectionFactory.createReflection();
        Reflection reflection2 = ReflectionFactory.createReflection();
        System.out.println(Arrays.toString(reflection.getClass().getDeclaredFields() ) );
        System.out.println(Arrays.toString(reflection2.getClass().getDeclaredMethods() ) );

    }
}
