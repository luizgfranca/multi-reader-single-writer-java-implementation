package io.github.luizgfranca.MRSWStore.impl;

import io.github.luizgfranca.MRSWStore.MRSWStore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnsafeMRSWStore<K, V> extends MRSWStore<K, V> {

    private final Map<K, V> mRegistry;

    public UnsafeMRSWStore() {
        super();
        mRegistry = new HashMap<>();
    }

    @Override
    public void publish(Map<K, V> data) {
        if (!isValidWriter(Thread.currentThread())) {
            System.out.println(Thread.currentThread().getName() + " is not owner, only " + mOwner.getName() + " can write ");
        };
        mRegistry.putAll(data);
    }

    @Override
    public List<V> get(List<K> keys) {
        return mRegistry
                .keySet()
                .stream()
                .filter(keys::contains)
                .map(mRegistry::get)
                .toList();
    }

}
