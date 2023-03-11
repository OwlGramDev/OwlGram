package it.owlgram.android.magic;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MagicHashMapVector<T, V> extends MagicBaseObject {
    HashMap<T, V> vector = new HashMap<>();

    public void put(T key, V value) {
        vector.put(key, value);
    }

    public V get(T key) {
        return vector.get(key);
    }

    public void remove(T key) {
        vector.remove(key);
    }

    public int size() {
        return vector.size();
    }

    public boolean containsKey(T key) {
        return vector.containsKey(key);
    }

    public boolean containsValue(V value) {
        return vector.containsValue(value);
    }

    @Override
    public int getConstructor() {
        return HASH_MAP_VECTOR_CONSTRUCTOR;
    }

    public Set<T> keySet() {
        return vector.keySet();
    }

    public Collection<V> values() {
        return vector.values();
    }

    public Set<Map.Entry<T, V>> entrySet() {
        return vector.entrySet();
    }
}
