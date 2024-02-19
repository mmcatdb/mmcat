/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.cuni.matfyz.mminfer.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * pridej hodnotu do bloom filteru - je to optimalizovany bloom filter, lazy
 * evaluace... nejprve pridavas postupne 10 000 cisel a teprve potom zacnes
 * pocitat pri merge a na konci musis overit, ze seznam cisel je prazdny, aby se
 * nestalo, ze neni spocitany
 *
 * @author pavel.koupil
 */
public class BloomFilter {

    private final List<Object> temp = new ArrayList<>();

    private long[] bloomFilter;

    private static HashFunction[] hashFunctions;

    private static Integer size = 0;

    private Integer count;

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
            this.addToFilter(value);
            // neni treba inkrementovat size, protoze v ELSE branch se == zmeni v >
        } else if (count < size) {
            temp.add(value);
        } else {
            this.temp();
        }
        ++count;
    }

    private void addToFilter(Object value) {
        for (HashFunction function : hashFunctions) {
            // WARN: apply musi byt volano nad stringem, tedy most generic value - hash funkce musi vzdy vratit stejny
            // vysledek kvuli union type
            Integer index = function.apply(value);
            bloomFilter[index] += 1L;
        }
    }

    public void temp() {
        this.bloomFilter = new long[size];
        for (Object val : temp) {
            this.addToFilter(val);
        }
        temp.clear();
    }

    public Integer getCount() {
        return count;
    }

    public void merge(BloomFilter filter) {
        if (count + filter.count > BloomFilter.size) {
            this.temp();
            filter.temp();
            this.merge(filter.bloomFilter);
            count += filter.count;
        } else if (count + filter.count <= BloomFilter.size) {
            this.merge(filter.temp);
            count += filter.count;
        }

    }

    private void merge(long[] anotherFilter) {
        for (int i = 0; i < BloomFilter.size; ++i) {
            bloomFilter[i] += anotherFilter[i];
        }
    }

    private void merge(List<Object> anotherTemp) {
        temp.addAll(anotherTemp);
    }

    public int isSubsetOf(BloomFilter other) {
        boolean isEqual = true;
        this.temp();
        other.temp();
        for (int i = 0; i < bloomFilter.length; i++) {
            if (bloomFilter[i] > other.bloomFilter[i]) {
                return -1;
            }
            if (bloomFilter[i] != other.bloomFilter[i]) {
                isEqual = false;
            }
        }
        return isEqual ? 0 : 1;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("BloomFilter{");
        sb.append("temp=").append(temp);
        sb.append(", bloomFilter=").append(bloomFilter);
        sb.append(", count=").append(count);
        sb.append('}');
        return sb.toString();
    }

}
