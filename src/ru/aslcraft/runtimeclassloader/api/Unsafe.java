package ru.aslcraft.runtimeclassloader.api;

public interface Unsafe {

    void putObject(Object o, long offset, Object x);

    void putInt(Object o, long offset, int x);

    Object getObject(Object o, long offset);
}
