package io.github.luizgfranca.MRSWStore;

import java.util.List;
import java.util.Map;

public abstract class MRSWStore<K, V> {

    protected Thread mOwner;


    protected boolean isValidWriter(Thread thread) {
        return thread.equals(mOwner);
    }

    public void claim(Thread owner) {
        mOwner = owner;
    }

    public abstract void publish(Map<K, V> data) throws InterruptedException;

    public abstract List<V> get(List<K> key) throws InterruptedException;

}
