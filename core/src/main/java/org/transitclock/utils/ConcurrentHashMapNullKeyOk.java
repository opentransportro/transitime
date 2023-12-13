/* (C)2023 */
package org.transitclock.utils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Inherits from ConcurrentHashMap but allows null key to be used. The methods that use the key are
 * overridden so that if null key is being used it is converted to a "". Since "" is used in place
 * of null the key must be a String.
 *
 * @author SkiBu Smith
 */
public class ConcurrentHashMapNullKeyOk<K, V> extends ConcurrentHashMap<K, V> {

    private static final long serialVersionUID = 6527928623559466566L;

    public ConcurrentHashMapNullKeyOk() {
        super();
    }

    public ConcurrentHashMapNullKeyOk(int initialCapacity) {
        super(initialCapacity);
    }

    public ConcurrentHashMapNullKeyOk(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public ConcurrentHashMapNullKeyOk(int initialCapacity, float loadFactor, int concurrencyLevel) {
        super(initialCapacity, loadFactor, concurrencyLevel);
    }

    /**
     * Notes that this only works for when the key is a String
     *
     * @param key
     * @return
     */
    @SuppressWarnings("unchecked")
    private K modKey(Object key) {
        return (K) (key != null ? key : "");
    }

    public V get(Object key) {
        return super.get(modKey(key));
    }

    public boolean containsKey(Object key) {
        return super.containsKey(modKey(key));
    }

    public V put(K key, V value) {
        return super.put(modKey(key), value);
    }

    public V putIfAbsent(K key, V value) {
        return super.putIfAbsent((K) modKey(key), value);
    }

    public V remove(Object key) {
        return super.remove(modKey(key));
    }

    public boolean remove(Object key, Object value) {
        return super.remove(modKey(key), value);
    }

    public V replace(K key, V value) {
        return super.replace(key, value);
    }

    public boolean replace(K key, V oldValue, V newValue) {
        return super.replace(key, oldValue, newValue);
    }
}
