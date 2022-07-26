package ru.aslcraft.runtimeclassloader.async;

import static ru.aslcraft.runtimeclassloader.async.Vavilon.Gates.*;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import ru.aslcraft.runtimeclassloader.api.Predicted;

final class PredictedImpl<R_TYPE> implements Predicted<R_TYPE> {

    private final AtomicBoolean workerIsFinished;
    private final long allocatedTime;

    private Set<Thread> threadCallers = new LinkedHashSet<>();

    private volatile long handledTime = -1;
    private volatile R_TYPE value;

    PredictedImpl(Thread threadCaller, AtomicBoolean workerIsFinished) {
        this.threadCallers.add(threadCaller);
        this.workerIsFinished = workerIsFinished;
        this.allocatedTime = System.currentTimeMillis();
    }

    void unlockThreads() {
        for (Thread caller : this.threadCallers) {
            getOpenGates().detachPredicted(caller, this);
            safeUnlockThread(caller);
        }
        /* Speed the release */
        this.threadCallers = null;
    }

    void internalSet(R_TYPE value) {
        this.value = value;
    }

    @Override
    public R_TYPE get() {
        // if not finished
        if (!workerIsFinished.get() ) {
            // called now from this thread
            Thread currThread = Thread.currentThread();
            // add itself to gate list
            getOpenGates().attachPredicted(currThread, this);
            // fill set with new threads
            this.threadCallers.add(currThread);
            // lock that thread
            safeLockThread(currThread);
        }

        // handle only first time receive
        if (handledTime == -1)
            this.handledTime = System.currentTimeMillis();

        return this.value;
    }

    @Override
    public long timeElapsed() {
        return this.timeElapsed(this.handledTime);
    }

    @Override
    public long timeElapsed(long currTime) {
        return currTime - this.allocatedTime;
    }

    @Override
    public String toString() {
        if (value == null) return "null";
        return this.value.toString();
    }
}
