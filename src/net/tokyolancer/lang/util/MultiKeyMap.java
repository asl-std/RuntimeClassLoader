package net.tokyolancer.lang.util;

import java.util.HashMap;
import java.util.Map;

public class MultiKeyMap<K1, K2, V> {

    private final Map<K1, V> map = new HashMap<>();

    private final Map<K2, V> otherMap = new HashMap<>();

    public void put(K1 key, K2 otherKey, V value) {
        if (key != null) map.put(key, value);
        if (otherKey != null) otherMap.put(otherKey, value);
    }

    public V get(K1 key, K2 otherKey) {
        if (map.containsKey(key) && otherMap.containsKey(otherKey) ) {
            if (map.get(key).equals(otherMap.get(otherKey) ) ) return map.get(key);
            else throw new AssertionError("Uncaught collision in two maps.");
        } else if (map.containsKey(key) ) return map.get(key);
        else return otherMap.getOrDefault(otherKey, null);
    }

    public V getByKey(K1 key) {
        return get(key, null);
    }

    public V getByOtherKey(K2 otherKey) {
        return get(null, otherKey);
    }
}
