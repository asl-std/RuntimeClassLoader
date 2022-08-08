package ru.aslcraft.runtimeclassloader.api.async.tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.aslcraft.runtimeclassloader.api.api.Predicted;
import ru.aslcraft.runtimeclassloader.api.util.GlueList;

public class PredictedList {

    private static final GlueList<Predicted<?>> EMPTY_LIST = new GlueList<>();

    private final Map<Thread, List<Predicted<?>>> currentMap = new HashMap<>();

    public PredictedList() {
        this(false);
    }

    public PredictedList(boolean loadCurrentThreads) {
        if (loadCurrentThreads)
            this.attachAllThreads();
    }

    private void attachAllThreads() {
        for (Thread thread : Thread.getAllStackTraces().keySet() )
            currentMap.put(thread, PredictedList.safeClone() );
    }

    public List<Predicted<?>> attachThread(Thread thread) {
        List<Predicted<?>> temp = PredictedList.safeClone();
        this.currentMap.putIfAbsent(thread, temp);
        return temp;
    }

    public void detachThread(Thread thread) {
        this.currentMap.remove(thread);
    }

    public void attachPredicted(Thread thread, Predicted<?> predicted) {
        this.currentMap.getOrDefault(thread, this.attachThread(thread) ).add(predicted);
    }

    public void detachPredicted(Thread thread, Predicted<?> predicted) {
        this.currentMap.getOrDefault(thread, this.attachThread(thread) ).remove(predicted);
    }

    public boolean isEmpty(Thread thread) {
        if (this.currentMap.isEmpty() ) return true;
        return this.currentMap.get(thread).isEmpty();
    }

    @SuppressWarnings("unchecked")
    private static List<Predicted<?>> safeClone() {
        return (List<Predicted<?>>) EMPTY_LIST.clone();
    }
}
