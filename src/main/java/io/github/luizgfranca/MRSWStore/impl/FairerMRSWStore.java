package io.github.luizgfranca.MRSWStore.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class FairerMRSWStore<K, V> extends SimpleMRSWStore<K, V> {

    private final Semaphore mChokeSemaphore = new Semaphore(1);

    @Override
    public void publish(Map<K, V> data) throws InterruptedException {
        mChokeSemaphore.acquire();
        mWriteSemaphore.acquire();
        mChokeSemaphore.release();
        if (!isValidWriter(Thread.currentThread())) {
            System.out.println(Thread.currentThread().getName() + " is not owner, only " + mOwner.getName() + " can write ");
            return;
        };

        mRegistry.putAll(data);
        mWriteSemaphore.release();
    }

    @Override
    public List<V> get(List<K> keys) throws InterruptedException {

        mChokeSemaphore.acquire();
        mChokeSemaphore.release();
        return super.get(keys);
    }
}
