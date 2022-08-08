package ru.aslcraft.api.dependency.loader.api;

/**
 *
 * TODO: end doc
 *
 * @since 1.0
 */
public interface Awaited<R_TYPE> {

    /**
     *
     * TODO: end doc
     *
     * @return Result of the function
     */
    Predicted<R_TYPE> await();

    Awaited<R_TYPE> asDaemon();

    Awaited<R_TYPE> withName(String name);

    Awaited<R_TYPE> withPriority(int priority);
}
