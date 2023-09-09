package io.github.luizgfranca.MRSWStore.impl;

import io.github.luizgfranca.MRSWStore.MRSWStore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class SimpleMRSWStore<K, V> extends MRSWStore<K, V> {

    protected final Map<K, V> mRegistry;

    protected long mReadersWaiting = 0;
    protected final Semaphore mWriteSemaphore = new Semaphore(1);
    protected final Semaphore mReadersWaitingSemaphore = new Semaphore(1);

    public SimpleMRSWStore() {
        super();
        try {
            mWriteSemaphore.acquire();
            mRegistry = new HashMap<>();
            mWriteSemaphore.release();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void claim(Thread owner) {
        try {
            mWriteSemaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        super.claim(owner);
        mWriteSemaphore.release();
    }

    @Override
    public void publish(Map<K, V> data) throws InterruptedException {
        mWriteSemaphore.acquire();
        if (!isValidWriter(Thread.currentThread())) {
            System.out.println(Thread.currentThread().getName() + " is not owner, only " + mOwner.getName() + " can write ");
            return;
        };

        mRegistry.putAll(data);
        mWriteSemaphore.release();
    }

    @Override
    public List<V> get(List<K> keys) throws InterruptedException {

        mReadersWaitingSemaphore.acquire();
        mReadersWaiting ++;
        if(mReadersWaiting == 1) {
            mWriteSemaphore.acquire();
        }
        mReadersWaitingSemaphore.release();


        final var values = mRegistry
                .keySet()
                .stream()
                .filter(keys::contains)
                .map(mRegistry::get)
                .toList();


        mReadersWaitingSemaphore.acquire();

        mReadersWaiting --;
        if(mReadersWaiting == 0) {

            mWriteSemaphore.release();
        }

        mReadersWaitingSemaphore.release();

        return values;
    }

}
