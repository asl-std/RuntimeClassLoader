package ru.aslcraft.runtimeclassloader.reflect;

import ru.aslcraft.runtimeclassloader.api.Reflection;
import ru.aslcraft.runtimeclassloader.unsafe.UnsafeFactory;
import ru.aslcraft.runtimeclassloader.unsafe.UnsafeImpl;
import ru.aslcraft.runtimeclassloader.util.RuntimeUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.*;

// Не советую убирать данную вещь или она испортит тебе глаза (я предупредил)
@SuppressWarnings("all")
final class ReflectionImpl implements Reflection {

    // used for cache
    private static final Map<ClassLoader, List<?>> cachedLoaders = new HashMap<>();

    // used for cache
    private static final Class<?>[] OLD_DATA = new Class<?>[] { String.class, byte[].class, int.class, int.class, ProtectionDomain.class, String.class };
    // used for cache
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

    // must be defined by constructor
    private static ReflectionImpl root = null;

    static {
        try {
            // Hiding classes from Java Reflection API
            ReflectionImpl.hideFromReflection(ReflectionImpl.class);
            ReflectionImpl.hideFromReflection(Offset.class);
            ReflectionImpl.hideFromReflection(UnsafeImpl.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // check if Reflection.class was loaded correctly (init only from ReflectionFactory)
    private boolean safeInit = false;

    ReflectionImpl() {
        ReflectionImpl.root = this; // caching it-self
        this.safeInit = true;
    }

    static ReflectionImpl cached() {
        if (ReflectionImpl.root == null)
            return new ReflectionImpl();
        return ReflectionImpl.root;
    }

    /**
     * Checks whether the current instance of the object is verified.
     * That means, that current instance initialized through the {@link ReflectionFactory} class
     */
    private void checkCaller() {
        if (this.root == null) throw new UnsupportedOperationException("Cannot be performed");
        if (!this.safeInit) throw new UnsupportedOperationException("Cannot be performed");
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
        this.checkCaller(); // check call
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
        UnsafeFactory.createUnsafe().putInt(method, Offset.of(Method.class, "modifiers"), mod);
    }

    /**
     *
     * Get the native method that is responsible for
     * initializing the class from the specified arguments.
     *
     * @return Needed method
     * @throws NoSuchMethodException This error cannot be throwned, but it will have to be handled (because I am pussy)
     */
    private static Method getDefineClassMethod() throws NoSuchMethodException {
        if (ReflectionImpl.defineClassMethod == null) {
            switch (RuntimeUtil.getRuntimeVersion() ) {
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
     * @return Class that successfully loaded
     * @throws NoSuchMethodException This error cannot be throwned, but it will have to be handled (because I am pussy)
     * @throws InvocationTargetException If the data or name is incorrect (check docs before using this method)
     * @throws IllegalAccessException This error cannot be throwned, but it will have to be handled (because I am pussy)
     */
    @Override
    public Class<?> defineClass(String name, byte[] data)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        this.checkCaller(); // check call
        switch (RuntimeUtil.getRuntimeVersion() ) {
            case 8:
                return (Class<?>) this.godInvoke(getDefineClassMethod(),
                        ClassLoader.getSystemClassLoader(), name, data, 0, data.length, null, null);
            case 16:
            case 17:
            default:
                return (Class<?>) this.godInvoke(getDefineClassMethod(), ClassLoader.getSystemClassLoader(),
                        ClassLoader.getSystemClassLoader(), name, data, 0, data.length, null, null);
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
    public boolean isClassPresents(String className, ClassLoader loader) {
        this.checkCaller(); // check call
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
        List<?> result = (List<?>) UnsafeFactory.createUnsafe().getObject(loader, Offset.of(ClassLoader.class, "classes") );
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
        this.checkCaller(); // check call
        UnsafeFactory.createUnsafe().putObject(clazz, Offset.of(Class.class, "classLoader"), loader);
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
        this.checkCaller(); // check call
        this.unlockNative(method); // will remove all modifiers
        Object tmp = UnsafeFactory.createUnsafe().getObject(method, Offset.of(Method.class, "clazz") );
        // Вот здесь, честное слово, магия ебейшая, я сам не знаю почему это работает, но оставлю это так
        // P.S. Причём я понял, что это не должно работать, только спустя некоторое время после релиза этого метода
        // P.S. Если будет проверка по совместимости с модулем - мы пролетим как фанера над Парижем
        UnsafeFactory.createUnsafe().putObject(method, Offset.of(Method.class, "clazz"), method.getDeclaringClass() );
        return method.invoke(o, args);
    }

    private static Class<?> getInternalReflectionClass()
            throws ClassNotFoundException {
        if (ReflectionImpl.internalReflectionClass == null) {
            switch (RuntimeUtil.getRuntimeVersion() ) {
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

    private static Method getHideFieldsMethod()
            throws ClassNotFoundException, NoSuchMethodException {
        if (ReflectionImpl.hideFieldsMethod == null) {
            switch (RuntimeUtil.getRuntimeVersion() ) {
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

    private static Method getHideMethodsMethod()
            throws ClassNotFoundException, NoSuchMethodException {
        if (ReflectionImpl.hideMethodsMethod == null) {
            switch (RuntimeUtil.getRuntimeVersion() ) {
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

    private static Object getWildcardObject() {
        if (ReflectionImpl.wildcardObject == null) {
            switch (RuntimeUtil.getRuntimeVersion() ) {
                case 8:
                    ReflectionImpl.wildcardObject = new String[] {
                            // fields from Reflection.java
                            "cachedLoaders", "OLD_DATA", "NEW_DATA",
                            "defineClassMethod", "hideMethodsMethod", "hideFieldsMethod",
                            "internalReflectionClass", "wildcardObject", "runtimeVersion",
                            "root", "safeInit",
                            // methods from Reflection.java
                            "getClasses", "defineClass", "lookup",
                            "setModifier", "unlockNative", "isClassPresents",
                            "godInvoke", "hideFromReflection", "getWildcardObject",
                            "redefineClassLoader", "getHideMethodsMethod", "getDefineClassMethod",
                            "getRuntimeVersion", "getHideFieldsMethod", "getInternalReflectionClass",
                            "cached", "checkCaller",
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
        if (RuntimeUtil.getRuntimeVersion() >= 16) {
            // Здесь происходит дичайшая магия сего интернет-пространства
            // Потому-что если мы попытается вызвать у класса, полученного через
            // getInternalReflectionClass(), метод getModule(), то версия Java SE 8
            // пошлёт нас далеко за горы и скажет, что такого метода нет.
            Class<?> internalClass = getInternalReflectionClass();
            // Поэтому достаём модуль по вычисленному оффсету из памяти JVM
            Object module = UnsafeFactory.createUnsafe().getObject(internalClass, Offset.of(Class.class, "module") );
            // Устанавливаем сами себе полученный модуль, тем самым делая подмену
            UnsafeFactory.createUnsafe().putObject(ReflectionImpl.class, Offset.of(Class.class, "module"), module);
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
