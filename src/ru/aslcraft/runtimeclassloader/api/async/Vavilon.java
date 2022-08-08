package ru.aslcraft.runtimeclassloader.api.async;

import java.lang.reflect.Constructor;
import java.util.concurrent.Callable;

import ru.aslcraft.runtimeclassloader.api.api.Awaited;
import ru.aslcraft.runtimeclassloader.api.async.tools.PredictedList;

@SuppressWarnings("unused")
public final class Vavilon {

    private Vavilon() { }

    private static final String CURR_VERSION = "1.9.8";
    private static final String IMPL_VERSION = Gates.CURR_VERSION; // Version of implemented 'Gates' class

    public static final class Gates {

        private static final PredictedList openGates = new PredictedList(true);

        private static final String CURR_VERSION = "1.1";
        private static final Class<?> CURR_GATES = AsyncWorker.class; // Class that will perform all needed calls

        private static final Constructor<?> CONSTRUCTOR_REFERENCE;

        static {
            try {
                CONSTRUCTOR_REFERENCE = CURR_GATES.getDeclaredConstructor(Thread.class, Callable.class);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        public static Awaited<Boolean> openAsync(Runnable runnable) {
            return Gates.openAsync(Vavilon.mutate(runnable) );
        }

        @SuppressWarnings("unchecked")
        public static <V> Awaited<V> openAsync(Callable<V> callable) {
            return (Awaited<V>) Gates.safeAllocator(Thread.currentThread(), callable);
        }

        private static Object safeAllocator(Thread thread, Callable<?> callable) {
            Object result;
            try {
                result = CONSTRUCTOR_REFERENCE.newInstance(thread, callable);
                Gates.openGates.attachThread(thread);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return result;
        }

        static void safeLockThread(Thread thread) {
            if (thread == null || !thread.isAlive() ) return;

            try {
                synchronized (thread) {
                    thread.wait();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        static void safeUnlockThread(Thread thread) {
            if (thread == null || !thread.isAlive() ) return;

            // wait when all references will be deleted
            if (!openGates.isEmpty(thread) ) return;

            synchronized (thread) {
                thread.notify();
            }
        }

        static PredictedList getOpenGates() {
            return Gates.openGates;
        }
    }

    /**
     *
     * Special modification of a standard lambda to a lambda type with a return value.
     *
     * @param runnable Runnable to mutate
     * @return Mutated runnable
     */
    private static Callable<Boolean> mutate(Runnable runnable) {
        return () -> {
            runnable.run();
            return true;
        };
    }
}
