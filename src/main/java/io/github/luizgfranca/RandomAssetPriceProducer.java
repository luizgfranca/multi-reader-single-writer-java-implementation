package io.github.luizgfranca;

import io.github.luizgfranca.MRSWStore.MRSWStore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RandomAssetPriceProducer implements Runnable {

    private final MRSWStore<String, AssetPriceInformation> mStore;
    private final List<String> mAssetIdsToGenerate;
    private final long mUpdateInterval;

    private Map<String, AssetPriceInformation> mAssets;

    private void updateValues() {
        final var newValues = mAssetIdsToGenerate.stream()
                .map(
                    (id) ->
                            mAssets.containsKey(id)
                                    ? mAssets.get(id)
                                    : new AssetPriceInformation(-1, Math.random() * 1000)
                )
                .map(
                    (information) -> new AssetPriceInformation(
                        information.generation() + 1,
                        information.value() + ((Math.random() - 0.5) * 10)
                    )
                )
                .toList()
                .iterator();

        mAssetIdsToGenerate.forEach(id -> mAssets.put(id, newValues.next()));
    }

    public void publish() {
        if(Main.LOG_ACTIONS)
            mAssets.forEach((key, value) -> System.out.println("PRODUCER: " + key + " -> " + value));

        try {
            mStore.publish(mAssets);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public RandomAssetPriceProducer(MRSWStore<String, AssetPriceInformation> store, List<String> assetIdsToGenerate, long updateIntervalMilliseconds) {

        mStore = store;
        if (Main.LOG_ACTIONS)
            System.out.println("RandomAssetPriceProducer store " + mStore.hashCode());
        mAssetIdsToGenerate = assetIdsToGenerate;
        mUpdateInterval = updateIntervalMilliseconds;
        mAssets = new HashMap<>();
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        mStore.claim(Thread.currentThread());

        while (System.currentTimeMillis() - startTime < Main.RUN_TIME_MS) {
            updateValues();
            publish();

            try {
                Thread.sleep(mUpdateInterval);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
