package cz.cuni.matfyz.core.utils;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jachymb.bartik
 */
public class Statistics {

    private static final Logger LOGGER = LoggerFactory.getLogger(Statistics.class);

    private static Map<Interval, Long> times = new TreeMap<>();
    private static Map<Interval, Long> starts = new TreeMap<>();

    public static void start(Interval interval) {
        starts.put(interval, System.nanoTime());
    }

    public static Long end(Interval interval) {
        var endTime = System.nanoTime();

        var startTime = starts.get(interval);
        if (startTime == null)
            return null;
        
        var difference = endTime - startTime;
        times.put(interval, difference);
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

        return (value == null ? "Null" : (value / 1000000 + " ms"));
    }

    public static String getInfo(Counter counter) {
        var value = get(counter);
        return printLargeInt(value);
    }

    private static String printLargeInt(long value) {
        if (value > 1000000000)
            return value / 1000000000 + "G";
        if (value > 1000000)
            return value / 1000000 + "M";
        if (value > 1000)
            return value / 1000 + "k";
        return value + "";
    }
    
    public static void logInfo(Interval interval) {
        LOGGER.info("{}\t({})", getInfo(interval), interval);
    }
    
    public static void logInfo(Counter counter) {
        LOGGER.info("{}\t({})", getInfo(counter), counter);
    }

    public enum Interval {
        MTC_ALGORIGHM,
        CTM_ALGORIGHM,
        DATABASE_TO_INSTANCE,
        INSTANCE_TO_DATABASE,
        JOIN,
        MOVE,
        IMPORT_JOIN_MOVE,
        JSON_LD_TO_RDF,
        RDF_TO_INSTANCE
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

}
