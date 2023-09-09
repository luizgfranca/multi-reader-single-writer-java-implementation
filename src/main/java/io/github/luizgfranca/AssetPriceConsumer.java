package io.github.luizgfranca;

import io.github.luizgfranca.MRSWStore.MRSWStore;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AssetPriceConsumer implements Runnable {

    private final MRSWStore<String, AssetPriceInformation> mStore;
    private final List<String> mAssetIdsToFollow;

    private final long mPollInterval;

    public AssetPriceConsumer(MRSWStore<String, AssetPriceInformation> store, List<String> assetIdsToFollow, long pollInterval) {
        mStore = store;
        mAssetIdsToFollow = assetIdsToFollow;
        mPollInterval = pollInterval;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < Main.RUN_TIME_MS) {

            try {
                final var values = mStore.get(mAssetIdsToFollow);

                if(values.size() == 0) {
                    if(mPollInterval > 0) {
                        Thread.sleep(mPollInterval);
                    }
                    continue;
                };

                final var data = Stream
                        .iterate(0, n -> n + 1)
                        .limit(mAssetIdsToFollow.size())
                        .collect(Collectors.toMap(mAssetIdsToFollow::get, values::get));

                data.forEach((id, priceInformation) -> {
                    if (Main.LOG_ACTIONS) {
                        System.out.println(
                                "CONSUMER" + Thread.currentThread().getName() + ": " + id + " -> " + priceInformation
                        );
                    }
                });

                final long referenceGeneration = data
                        .values()
                        .stream()
                        .findFirst()
                        .orElseThrow(RuntimeException::new).generation();

                if (!data
                        .values()
                        .stream()
                        .allMatch((priceInformation) ->
                                priceInformation == null || priceInformation.generation() == referenceGeneration
                        )
                ) {
                    System.out.println("********** PROBLEM! INCONSISTENT GENERATION **********");
                    throw new RuntimeException();
                }

                if(mPollInterval > 0) {
                    Thread.sleep(mPollInterval)
                };
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
