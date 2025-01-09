package cz.matfyz.core.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Statistics {

    private static final Logger LOGGER = LoggerFactory.getLogger(Statistics.class);

    private static Map<Interval, Long> times = new TreeMap<>();
    private static Map<Interval, Long> starts = new TreeMap<>();

    public static void start(Interval interval) {
        starts.put(interval, System.nanoTime());
    }

    public static Long end(Interval interval) {
        final var endTime = System.nanoTime();

        final var startTime = starts.get(interval);
        if (startTime == null)
            return null;

        final var difference = endTime - startTime;

        final var current = times.get(interval);
        times.put(interval, (current != null ? current : 0) + difference);
        starts.put(interval, null);

        return difference;
    }

    public static Long get(Interval interval) {
        return times.get(interval);
    }

    public static long get(Counter counter) {
        return Optional.ofNullable(counters.get(counter)).orElse(0L);
    }

    public static void reset(Interval interval) {
        times.put(interval, null);
        starts.put(interval, null);
    }

    public static void reset(Counter counter) {
        counters.put(counter, 0L);
    }

    public static void reset() {
        times.clear();
        starts.clear();
        counters = generateCounters();
    }


    public static String getInfo(Interval interval) {
        var value = times.get(interval);

        return value == null ? "Null" : printNanoseconds(value);
    }

    public static String getInfo(Counter counter) {
        var value = get(counter);
        return printLargeInt(value);
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

    public static void logInfo(Interval interval) {
        LOGGER.info("{}\t({})", getInfo(interval), interval);
    }

    public static void logInfo(Counter counter) {
        LOGGER.info("{}\t({})", getInfo(counter), counter);
    }

    public enum Interval {
        MTC_ALGORITHM,
        CTM_ALGORITHM,
        DATABASE_TO_INSTANCE,
        INSTANCE_TO_DATABASE,
        JOIN,
        MOVE,
        IMPORT_JOIN_MOVE,
        JSON_LD_TO_RDF,
        RDF_TO_INSTANCE,
        PREPARE,
        PROCESS,
    }

    private static Map<Counter, Long> counters = generateCounters();

    public static long increment(Counter counter) {
        var value = get(counter);
        value++;
        counters.put(counter, value);

        return value;
    }

    public static void set(Counter counter, long value) {
        counters.put(counter, value);
    }

    public enum Counter {
        PULLED_RECORDS,
        CREATED_STATEMENTS,
        JOIN_ROWS,
        MOVE_ROWS
    }

    private static Map<Counter, Long> generateCounters() {
        var map = new TreeMap<Counter, Long>();

        for (var counter : Counter.values())
            map.put(counter, 0L);

        return map;
    }

    public abstract static class Aggregator {

        private Aggregator() {}

        private static final List<Map<Interval, Long>> runs = new ArrayList<>();
        private static final Map<Interval, Long> currentRun = new TreeMap<>();

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

        public static String getInfo(Interval interval) {
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

        public static void logInfo(Interval interval) {
            LOGGER.info("\n{}", getInfo(interval));
        }

    }

}
