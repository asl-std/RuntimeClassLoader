package net.tokyolancer.lang.reflect;

import net.tokyolancer.lang.api.Reflection;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.*;

import static sun.misc.Unsafe.getUnsafe;

// Не советую убирать данную вещь или она испортит тебе глаза (я предупредил)
@SuppressWarnings("all")
final class ReflectionImpl extends Reflection {

    // Более быстрый способ для получения значений
    private static final Map<ClassLoader, List<?>> cachedLoaders = new HashMap<>();

    // Так называемые 'var-args' которые необходимы для определения метода инициализации класса из байтов
    private static final Class<?>[] OLD_DATA = new Class<?>[] { String.class, byte[].class, int.class, int.class, ProtectionDomain.class, String.class };
    private static final Class<?>[] NEW_DATA = new Class<?>[] { ClassLoader.class, String.class, byte[].class, int.class, int.class, ProtectionDomain.class, String.class };

    // used for cache
    private static Method defineClassMethod;
    // used for cache
    private static Method hideMethodsMethod;
    // used for cache
    private static Method hideFieldsMethod;
    // used for cache
    private static Class<?> internalReflectionClass;
    // used for cache
    private static Object wildcardObject;
    // used for cache
    private static int runtimeVersion = -1;

    // must be defined by constructor
    private static ReflectionImpl root = null;

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            // Now unsafe can be accessed directly by method getUnsafe();
            ((Unsafe) field.get(null) ).
                    putObject(ReflectionImpl.class, Offset.of(Class.class, "classLoader"), null);
            // Hiding classes from Java Reflection API
            ReflectionImpl.hideFromReflection(ReflectionImpl.class);
            ReflectionImpl.hideFromReflection(Offset.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // caching it-self
    ReflectionImpl() { ReflectionImpl.root = this; }

    static @Cached ReflectionImpl cached() {
        if (ReflectionImpl.root == null)
            return new ReflectionImpl();
        return ReflectionImpl.root;
    }

    @Override
    public Unsafe lookup() { return getUnsafe(); }

    /**
     *
     * Retrieves the current version of the JVM.
     *
     * @return Runtime version in parsed view (8 - JAVA 1.8, 16 - JAVA 16 and etc.)
     */
    static @Cached int getRuntimeVersion() {
        if (ReflectionImpl.runtimeVersion == -1) {
            String version = System.getProperty("java.version");
            if (version.startsWith("1.") ) version = version.substring(2, 3);
            else {
                int dot = version.indexOf(".");
                if (dot != -1) version = version.substring(0, dot);
            }
            ReflectionImpl.runtimeVersion = Integer.parseInt(version);
        }
        return ReflectionImpl.runtimeVersion;
    }

    /**
     *
     * Fixes a problem with calling a method that
     * contains a native keyword via the Java Reflection API
     *
     * @param method The method to perform on
     */
    @Override
    public void unlockNative(Method method) {
        ReflectionImpl.setModifier(method, Modifier.PUBLIC);
    }

    /**
     *
     * Allows you to put any available modifier for the specified method.
     *
     * @param method The method to perform on
     * @param mod The modifier to set
     */
    private static void setModifier(Method method, int mod) {
        getUnsafe().putInt(method, Offset.of(Method.class, "modifiers"), mod);
    }

    /**
     *
     * Get the native method that is responsible for
     * initializing the class from the specified arguments.
     *
     * @return Needed method
     * @throws NoSuchMethodException This error cannot be throwned, but it will have to be handled (because I am pussy)
     */
    private static @Cached Method getDefineClassMethod() throws NoSuchMethodException {
        if (ReflectionImpl.defineClassMethod == null) {
            switch (ReflectionImpl.getRuntimeVersion() ) {
                case 8:
                    ReflectionImpl.defineClassMethod = ClassLoader.class.getDeclaredMethod("defineClass1", ReflectionImpl.OLD_DATA);
                    break;
                case 16:
                case 17:
                default:
                    ReflectionImpl.defineClassMethod = ClassLoader.class.getDeclaredMethod("defineClass1", ReflectionImpl.NEW_DATA);
                    break;
            }
        }
        return ReflectionImpl.defineClassMethod;
    }

    /**
     *
     * Allows you to initialize a class with the correct name
     * and automatically load it into runtime in SystemClassLoader loader
     *
     * @param name Correct class name
     * @param data Correct class byte data
     * @throws NoSuchMethodException This error cannot be throwned, but it will have to be handled (because I am pussy)
     * @throws InvocationTargetException If the data or name is incorrect (check docs before using this method)
     * @throws IllegalAccessException This error cannot be throwned, but it will have to be handled (because I am pussy)
     */
    @Override
    public void defineClass(String name, byte[] data)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        switch (ReflectionImpl.getRuntimeVersion() ) {
            case 8:
                this.godInvoke(getDefineClassMethod(),
                        ClassLoader.getSystemClassLoader(), name, data, 0, data.length, null, null);
                break;
            case 16:
            case 17:
            default:
                this.godInvoke(getDefineClassMethod(), ClassLoader.getSystemClassLoader(),
                        ClassLoader.getSystemClassLoader(), name, data, 0, data.length, null, null);
                break;
        }
    }

    /**
     *
     * Checks whether the class is loaded in the specified class loader
     *
     * @param className Class name to check
     * @param loader Exact class loader
     * @return If loaded - true, otherwise - false
     */
    @Override
    public @Cached boolean isClassPresents(String className, ClassLoader loader) {
        List<?> fetched = ReflectionImpl.cachedLoaders.get(loader);
        if (fetched == null) fetched = ReflectionImpl.getClasses(loader);
        for (Object o : fetched) if (className.equals(((Class<?>) o).getName() ) ) return true;
        return false;
    }

    /**
     *
     * Retrieves the list of loaded classes in the specified class loader
     *
     * @param loader Exact class loader
     * @return List of loaded classes
     */
    private static List<?> getClasses(ClassLoader loader) {
        List<?> result = (List<?>) getUnsafe().getObject(loader, Offset.of(ClassLoader.class, "classes") );
        ReflectionImpl.cachedLoaders.put(loader, result);
        return result;
    }

    /**
     *
     * Allows you to change the class loader of the specified class to any other loader
     *
     * @param clazz Exact class
     * @param loader Exact class loader
     */
    @Override
    public void redefineClassLoader(Class<?> clazz, ClassLoader loader) {
        getUnsafe().putObject(clazz, Offset.of(Class.class, "classLoader"), loader);
    }

    /**
     * Allows you to call invoke on an instance of a method with <b>all privileges</b>
     *
     * @param method The method to be executed
     * @param o The object the underlying method is invoked from
     * @param args The arguments used for the method call
     * @return The result of dispatching the method represented by this object on obj with parameters args
     * @throws InvocationTargetException If the underlying method throws an exception
     * @throws IllegalAccessException If this Method object is enforcing Java language access control and the underlying method is inaccessible
     */
    @Override
    public Object godInvoke(Method method, Object o, Object... args)
            throws InvocationTargetException, IllegalAccessException {
        this.unlockNative(method); // will remove all modifiers
        Object tmp = getUnsafe().getObject(method, Offset.of(Method.class, "clazz") );
        // Вот здесь, честное слово, магия ебейшая, я сам не знаю почему это работает, но оставлю это так
        // P.S. Причём я понял, что это не должно работать, только спустя некоторое время после релиза этого метода
        // P.S. Если будет проверка по совместимости с модулем - мы пролетим как фанера над Парижем
        getUnsafe().putObject(method, Offset.of(Method.class, "clazz"), method.getDeclaringClass() );
        return method.invoke(o, args);
    }

    private static @Cached Class<?> getInternalReflectionClass()
            throws ClassNotFoundException {
        if (ReflectionImpl.internalReflectionClass == null) {
            switch (ReflectionImpl.getRuntimeVersion() ) {
                case 8:
                    ReflectionImpl.internalReflectionClass = Class.forName("sun.reflect.Reflection");
                    break;
                case 16:
                case 17:
                default:
                    ReflectionImpl.internalReflectionClass = Class.forName("jdk.internal.reflect.Reflection");
                    break;
            }
        }
        return ReflectionImpl.internalReflectionClass;
    }

    private static @Cached Method getHideFieldsMethod()
            throws ClassNotFoundException, NoSuchMethodException {
        if (ReflectionImpl.hideFieldsMethod == null) {
            switch (ReflectionImpl.getRuntimeVersion() ) {
                case 8:
                    ReflectionImpl.hideFieldsMethod = getInternalReflectionClass()
                            .getDeclaredMethod("registerFieldsToFilter", Class.class, String[].class);
                    break;
                case 16:
                case 17:
                default:
                    ReflectionImpl.hideFieldsMethod = getInternalReflectionClass().
                            getDeclaredMethod("registerFieldsToFilter", Class.class, Set.class);
                    break;
            }
        }
        return ReflectionImpl.hideFieldsMethod;
    }

    private static @Cached Method getHideMethodsMethod()
            throws ClassNotFoundException, NoSuchMethodException {
        if (ReflectionImpl.hideMethodsMethod == null) {
            switch (ReflectionImpl.getRuntimeVersion() ) {
                case 8:
                    ReflectionImpl.hideMethodsMethod = getInternalReflectionClass()
                            .getDeclaredMethod("registerMethodsToFilter", Class.class, String[].class);
                    break;
                case 16:
                case 17:
                default:
                    ReflectionImpl.hideMethodsMethod = getInternalReflectionClass().
                            getDeclaredMethod("registerMethodsToFilter", Class.class, Set.class);
                    break;
            }
        }
        return ReflectionImpl.hideMethodsMethod;
    }

    private static @Cached Object getWildcardObject() {
        if (ReflectionImpl.wildcardObject == null) {
            switch (ReflectionImpl.getRuntimeVersion() ) {
                case 8:
                    ReflectionImpl.wildcardObject = new String[] {
                            // fields from Reflection.java
                            "cachedLoaders", "OLD_DATA", "NEW_DATA",
                            "defineClassMethod", "hideMethodsMethod", "hideFieldsMethod",
                            "internalReflectionClass", "wildcardObject", "runtimeVersion",
                            "root",
                            // methods from Reflection.java
                            "getClasses", "defineClass", "lookup",
                            "setModifier", "unlockNative", "isClassPresents",
                            "godInvoke", "hideFromReflection", "getWildcardObject",
                            "redefineClassLoader", "getHideMethodsMethod", "getDefineClassMethod",
                            "getRuntimeVersion", "getHideFieldsMethod", "getInternalReflectionClass",
                            "cached",
                            // fields from Offset.java
                            "offsets",
                            // methods from Offset.java
                            "of"
                    };
                    break;
                case 16:
                case 17:
                default:
                    Set<String> set = new HashSet<>();
                    set.add("*");
                    ReflectionImpl.wildcardObject = set;
                    break;
            }
        }
        return ReflectionImpl.wildcardObject;
    }

    /**
     *
     * Allows you to hide the specified class from the eyes of Java developers.
     * Completely hides all fields and methods in the class.
     * Also, the data cannot be unhide again (a one-time promotion, that's how we live).
     *
     * @param clazz Exact class that will be hided from Java Reflection API
     * @throws ClassNotFoundException This error cannot be throwned, but it will have to be handled (because I am pussy)
     * @throws NoSuchMethodException This error cannot be throwned, but it will have to be handled (because I am pussy)
     * @throws InvocationTargetException If you try to use this method twice on one exact class
     * @throws IllegalAccessException This error cannot be throwned, but it will have to be handled (because I am pussy)
     */
    private static void hideFromReflection(Class<?> clazz)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (getRuntimeVersion() >= 16) {
            // Здесь происходит дичайшая магия сего интернет-пространства
            // Потому-что если мы попытается вызвать у класса, полученного через
            // getInternalReflectionClass(), метод getModule(), то версия Java SE 8
            // пошлёт нас далеко за горы и скажет, что такого метода нет.
            Class<?> internalClass = getInternalReflectionClass();
            // Поэтому достаём модуль по вычисленному оффсету из памяти JVM
            Object module = getUnsafe().getObject(internalClass, Offset.of(Class.class, "module") );
            // Устанавливаем сами себе полученный модуль, тем самым делая подмену
            getUnsafe().putObject(ReflectionImpl.class, Offset.of(Class.class, "module"), module);
        }
        // В общем, есть такое волшебное поле - override в классе
        // AccessibleObject и оно влияет на то, будет ли вызываться проверка
        // на разные штуки (вроде совместимости пакейджа, модиферов и прочего)
        //
        // Так-как в JAVA 8 видимо не думали о том, что появятся такие гении как мы
        // (которые захотят в рантайме грузить кастом классы и редефайнить класслоадеры),
        // то штучка выше (на подмену модуля) не нужна и можно просто снять весь протект.
        getHideFieldsMethod().setAccessible(true);
        getHideMethodsMethod().setAccessible(true);
        getHideFieldsMethod().invoke(null, clazz, getWildcardObject() );
        getHideMethodsMethod().invoke(null, clazz, getWildcardObject() );
    }
}
