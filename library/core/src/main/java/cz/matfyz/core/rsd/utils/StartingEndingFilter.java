package cz.matfyz.core.rsd.utils;

import cz.matfyz.core.rsd.helpers.CharToIndexConverter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StartingEndingFilter implements Serializable {

        // 0-9, a-z, speciální znaky
        private int[] startingArray;

        // (0-9, a-z, speciální znaky) * 37 + (0-9, a-z, speciální znaky)
        private int[] startingEndingArray;

        private static int size;

        private List<Integer> tempStartingIndexes = new ArrayList<>();
        private List<Integer> tempStartingEndingIndexes = new ArrayList<>();

        private int count;

        public static void setParams(int size) {
            StartingEndingFilter.size = size;
    }

        public StartingEndingFilter() {
            this.count = 0;
        }

        public void add(Object value) {
            if (count > size) {
                addToFilter(value);
            }
            else if (count < size) {
                tempAdd(value.toString());
            }
            else {
                temp();
            }
            ++count;
        }

        private void addToFilter(Object value) {
            char first;
            char last;
            String stringRepresentation = value.toString();

            if (stringRepresentation.equals("") || stringRepresentation.equals("-")) {
                return;
            }
            if (stringRepresentation.charAt(0) != '-') {
                first = stringRepresentation.charAt(0);
            }
            else {
                first = stringRepresentation.charAt(1);
            }
            last = stringRepresentation.charAt(stringRepresentation.length() - 1);
            ++startingArray[CharToIndexConverter.INSTANCE.convert(first)];
            ++startingEndingArray[CharToIndexConverter.INSTANCE.convertWithEnding(first, last)];
        }

        private void tempAdd(String string) {
            char first;
            char last;

            if (string.equals("") || string.equals("-")) {
                return;
            }
            if (string.charAt(0) != '-') {
                first = string.charAt(0);
            }
            else {
                first = string.charAt(1);
            }
            last = string.charAt(string.length() - 1);
            tempStartingIndexes.add(CharToIndexConverter.INSTANCE.convert(first));
            tempStartingEndingIndexes.add(CharToIndexConverter.INSTANCE.convertWithEnding(first, last));
        }

        public void temp() {
        this.startingArray = new int[37];
        this.startingEndingArray = new int[37 * 37];
        for (Integer index : tempStartingIndexes) {
            ++startingArray[index];
        }
        for (Integer index : tempStartingEndingIndexes) {
            ++startingEndingArray[index];
        }
        tempStartingIndexes.clear();
        tempStartingEndingIndexes.clear();
    }

        public void merge(StartingEndingFilter other) {
            if (count + other.count > StartingEndingFilter.size) {
                this.temp();
                other.temp();
                for (int i = 0; i < startingArray.length; ++i) {
                    startingArray[i] += other.startingArray[i];
                }
                for (int i = 0; i < startingEndingArray.length; ++i) {
                    startingEndingArray[i] += other.startingEndingArray[i];
                }
                this.count += other.count;
            }
            else {
                tempStartingIndexes.addAll(other.tempStartingIndexes);
                tempStartingEndingIndexes.addAll(other.tempStartingEndingIndexes);
                count += other.count;
            }
        }

        public int[] getStartingArray() {
            return this.startingArray;
        }

        public int[] getStartingEndingArray() {
            return this.startingEndingArray;
        }

        @Override
        public String toString() {
            return "{starting array: " + Arrays.toString(startingArray)
                + ", strating ending array: " + Arrays.toString(startingEndingArray)
                + ", tempStartingIndexes: " + tempStartingIndexes
                + ", tempStartingEndingIndexes: " + tempStartingEndingIndexes
                + "}";
        }
}
