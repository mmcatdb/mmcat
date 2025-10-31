package cz.matfyz.core.rsd.utils;

import cz.matfyz.core.rsd.utils.Hashing.HashFunction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// FIXME This comment.
/**
 * pridej hodnotu do bloom filteru - je to optimalizovany bloom filter, lazy
 * evaluace... nejprve pridavas postupne 10 000 cisel a teprve potom zacnes
 * pocitat pri merge a na konci musis overit, ze seznam cisel je prazdny, aby se
 * nestalo, ze neni spocitany
 */
public class BloomFilter implements Serializable {

    private final List<Integer> temp = new ArrayList<>();

    private long[] bloomFilter;
    private static HashFunction[] hashFunctions;
    private static int size = 0;
    private int count;

    public static void setParams(int size, HashFunction... hashFunctions) {
        BloomFilter.size = size;
        BloomFilter.hashFunctions = hashFunctions;
    }

    public BloomFilter() {
        count = 0;
        //bloomFilter = new long[size];
    }

    public void add(Object value) {
        if (count > size) {
            for (HashFunction function : hashFunctions)
                this.addToFilter(function.apply(value));
            // neni treba inkrementovat size, protoze v ELSE branch se == zmeni v >
        }
        else if (count < size) {
            for (HashFunction function : hashFunctions)
                temp.add(function.apply(value));
        }
        else {
            this.temp();
        }
        count++;
    }

    private void addToFilter(int index) {
        for (HashFunction function : hashFunctions) {
            // WARN: apply musi byt volano nad stringem, tedy most generic value - hash funkce musi vzdy vratit stejny
            // vysledek kvuli union type
            bloomFilter[index] += 1L;
        }
    }

    public void temp() {
        this.bloomFilter = new long[size];
        for (Integer index : temp) {
            this.addToFilter(index);
        }
        temp.clear();
    }

    public int getCount() {
        return count;
    }

    public void merge(BloomFilter filter) {
        if (count + filter.count > BloomFilter.size) {
            this.temp();
            filter.temp();
            this.merge(filter.bloomFilter);
            count += filter.count;
        }
        else if (count + filter.count <= BloomFilter.size) {
            this.merge(filter.temp);
            count += filter.count;
        }

    }

    private void merge(long[] anotherFilter) {
        for (int i = 0; i < BloomFilter.size; i++)
            bloomFilter[i] += anotherFilter[i];
    }

    private void merge(List<Integer> anotherTemp) {
        temp.addAll(anotherTemp);
    }

    @Override public String toString() {
        return "BloomFilter{" + "temp=" + temp + ", bloomFilter=" + Arrays.toString(bloomFilter) + ", count=" + count + '}';
    }

    public int isSubsetOf(BloomFilter other) {
        boolean isEqual = true;
        this.temp();
        other.temp();
        for (int i = 0; i < bloomFilter.length; i++) {
            if (bloomFilter[i] > other.bloomFilter[i])
                return -1;
            if (bloomFilter[i] != other.bloomFilter[i])
                isEqual = false;
        }
        return isEqual ? 0 : 1;
    }

}
