package net.tokyolancer.lang.api;

/**
 *
 * TODO: end doc
 *
 * @since 1.0
 * @param <R_TYPE> Special type of variable
 */
public interface Predicted<R_TYPE> {

    R_TYPE get();

    long timeElapsed();

    long timeElapsed(long currTime);
}
