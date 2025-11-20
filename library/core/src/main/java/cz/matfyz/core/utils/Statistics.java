package cz.matfyz.core.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Statistics {

    private static final Logger LOGGER = LoggerFactory.getLogger(Statistics.class);

    private static Map<String, Long> starts = new TreeMap<>();
    private static Map<String, Long> times = new TreeMap<>();
    private static Map<String, Long> counters = new TreeMap<>();

    public static void start(String interval) {
        starts.put(interval, System.nanoTime());
    }

    public static long end(String interval) {
        final var endTime = System.nanoTime();

        final var startTime = starts.get(interval);
        if (startTime == null)
            throw new RuntimeException("Interval '" + interval + "' was not started.");

        final var difference = endTime - startTime;

        final var current = times.get(interval);
        times.put(interval, (current != null ? current : 0) + difference);
        starts.put(interval, null);

        return difference;
    }

    public static long increment(String counter) {
        var value = getCounter(counter);
        value++;
        counters.put(counter, value);

        return value;
    }

    public static void set(String counter, long value) {
        counters.put(counter, value);
    }

    public static void resetInterval(String interval) {
        times.put(interval, null);
        starts.put(interval, null);
    }

    public static void resetCounter(String counter) {
        counters.put(counter, 0L);
    }

    public static void reset() {
        times.clear();
        starts.clear();
        counters.clear();
    }

    public static long getInterval(String interval) {
        final var output = times.get(interval);
        if (output == null)
            throw new RuntimeException("Interval '" + interval + "' was not ended.");

        return output;
    }

    public static long getCounter(String counter) {
        final var output = counters.get(counter);
        return output == null ? 0L : output;
    }

    public static String getIntervalInfo(String interval) {
        final var value = times.get(interval);
        return value == null ? "Null" : printNanoseconds(value);
    }

    public static String getCounterInfo(String counter) {
        final var value = getCounter(counter);
        return printLargeInt(value);
    }

    public static void logIntervalInfo(String interval) {
        LOGGER.info("{}\t({})", getIntervalInfo(interval), interval);
    }

    public static void logCounterInfo(String counter) {
        LOGGER.info("{}\t({})", getCounterInfo(counter), counter);
    }

    private static String printLargeInt(long value) {
        if (value > 10000000000L)
            return value / 1000000000 + "G";
        if (value > 10000000L)
            return value / 1000000 + "M";
        if (value > 10000L)
            return value / 1000 + "k";

        return value + "";
    }

    private static String printNanoseconds(long value) {
        if (value > 10000000000L)
            return value / 1000000000 + " s";
        if (value > 10000000L)
            return value / 1000000 + " ms";
        if (value > 10000L)
            return value / 1000 + " μs";

        return value + " ns";
    }

    public abstract static class Aggregator {

        private Aggregator() {}

        private static final List<Map<String, Long>> runs = new ArrayList<>();
        private static final Map<String, Long> currentRun = new TreeMap<>();

        public static void collectBatch() {
            times.keySet().forEach(interval -> {
                final var value = times.get(interval);
                final var currentRaw = currentRun.get(interval);
                final var current = currentRaw != null ? currentRaw : 0L;

                currentRun.put(interval, current + value);
            });
            Statistics.reset();
        }

        public static void collectRun() {
            runs.add(new TreeMap<>(currentRun));
            currentRun.clear();
            Statistics.reset();
        }

        public static void reset() {
            runs.clear();
            currentRun.clear();
            Statistics.reset();
        }

        public static String getInfo(String interval) {
            long count = runs.stream().filter(r -> r.containsKey(interval)).count();
            long total = runs.stream().filter(r -> r.containsKey(interval)).mapToLong(r -> r.get(interval)).sum();

            final var values = runs.stream().map(r -> {
                final var value = r.get(interval);
                return value == null ? "Null" : printNanoseconds(value);
            }).toList();

            return String.format("- interval: %s\n- count: %s\n- total: %s\n- average: %s\n- values: [ %s ]",
                interval,
                count,
                printNanoseconds(total),
                count != 0 ? printNanoseconds(total / count) : "NaN",
                String.join(", ", values)
            );
        }

        public static void logInfo(String interval) {
            LOGGER.info("\n{}", getInfo(interval));
        }

    }

}
