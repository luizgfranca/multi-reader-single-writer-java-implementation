package io.github.luizgfranca;

import io.github.luizgfranca.MRSWStore.impl.FairerMRSWStore;
import io.github.luizgfranca.MRSWStore.impl.SimpleMRSWStore;
import io.github.luizgfranca.MRSWStore.impl.UnsafeMRSWStore;

import java.util.List;
import java.util.stream.Stream;

public class Main {

    public static final long RUN_TIME_MS = 10000;
    public static final long UPDATE_INTERVAL = 1;
    public static final long POLL_INTERVAL = 0;
    public static final long NUMBER_OF_CONSUMERS = 15;
    public static final List<String> ASSET_TAGS = List.of("GOOGL", "APPL", "MSFT");

    public static final boolean LOG_ACTIONS = true;

    public static void main(String[] args) {

//        final var store = new UnsafeMRSWStore<String, AssetPriceInformation>();
        final var store = new FairerMRSWStore<String, AssetPriceInformation>();


            final var producer = new Thread(
                new RandomAssetPriceProducer(
                    store,
                    ASSET_TAGS,
                    UPDATE_INTERVAL
                )
        );

        final var consumers = Stream
                .iterate(0, n -> n + 1)
                .limit(NUMBER_OF_CONSUMERS)
                .map(x -> new AssetPriceConsumer(
                        store,
                        ASSET_TAGS,
                        POLL_INTERVAL
                ))
                .map(Thread::new)
                .toList();


        producer.start();
        consumers.forEach(Thread::start);

        try {
            producer.join();
            for (Thread consumer : consumers) { consumer.join(); }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}