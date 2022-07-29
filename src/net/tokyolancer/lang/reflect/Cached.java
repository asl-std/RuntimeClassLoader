package net.tokyolancer.lang.reflect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation indicates to the developer that this function is a cache function,
 * that is, the result of the method will be cached (saved) by the class and the next time it is called,
 * it will immediately output the saved object data.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cached { }
