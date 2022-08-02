package net.tokyolancer.lang.async;

import net.tokyolancer.lang.api.Awaited;
import net.tokyolancer.lang.api.Predicted;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

final class AsyncWorker<R_TYPE> extends Thread implements Awaited<R_TYPE> {

    private AtomicBoolean isFinished = new AtomicBoolean(false);
    private Callable<R_TYPE> callable;
    private PredictedImpl<R_TYPE> predictedObject;

    AsyncWorker(Thread parentThread, Callable<R_TYPE> callable) {
        this.predictedObject = new PredictedImpl<>(parentThread, isFinished);
        this.callable = callable;
    }

    @Override
    public void run() {
        try {
            this.predictedObject.internalSet(this.callable.call() );
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.isFinished.set(true);
            this.predictedObject.unlockThreads();
            /* Speed the release of some of these resources */
            this.isFinished = null;
            this.callable = null;
            this.predictedObject = null;
        }
    }

    @Override
    public Predicted<R_TYPE> await() {
        this.start();
        return predictedObject;
    }

    @Override
    public Awaited<R_TYPE> asDaemon() {
        this.setDaemon(true);
        return this;
    }

    @Override
    public Awaited<R_TYPE> withName(String name) {
        this.setName(name);
        return this;
    }

    @Override
    public Awaited<R_TYPE> withPriority(int priority) {
        this.setPriority(priority);
        return this;
    }
}
