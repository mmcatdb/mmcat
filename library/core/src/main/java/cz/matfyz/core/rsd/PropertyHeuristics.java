package cz.matfyz.core.rsd;

import cz.matfyz.core.rsd.utils.Hashing;
import cz.matfyz.core.rsd.utils.BloomFilter;
import cz.matfyz.core.rsd.utils.StartingEndingFilter;
import java.io.Serializable;
import java.sql.Blob;
import java.sql.Clob;

import org.checkerframework.checker.nullness.qual.Nullable;

public class PropertyHeuristics implements Serializable {

    // TODO fix this shit
    // jeste vedle musi bezet jeden job, ktery spocita pocty records
    // tedy mas funkci MapColumns
    // a pak MapRows - pro pocet recordu, abychom dokazali overit pocet unique a not null (minimalni pocty)?
    // moment, musis uvazovat pole of simple types! tohle to rozbije, protoze by je row pocital jako 1, ale mohly by pridat 3 unikatni hodnoty a jinde by bylo 2x null, a tedy vysledek
    // by byl nespravne unique + not null...
    // pro min, max, average, bloom filter POTREBUJES nutne distinct hodnoty, a to distinct hodnoty nad namapovanymi sloupci
    // COUNT UZ ZISKAS Z ODVOZOVANI SCHEMATU... POKUD POUZIJES UNIVERSAL APPROACH! ALE KDYZ NE, TAK TO NEJDE POUZIT...
    // unique spoctes pouze pres mapreduce, pro kazdy sloupec ... ale nejde mit pair<pair<...>> takze to take neni reseni
    // co kdybys dal hodnotu do MyKey... pak to muzes redukovat pomoci klice -> pak MyKey namapujes na neco jineho a muzes udelat distinct, pricemz si zachovas informaci o tom
    // kolikrat tam ktera hodnota je
    // a pri slucovani, pokud nektera hodnota je vicekrat, tak se eliminuje
    // musi se to udelat pred distinct
    public final String hierarchicalName;

    /** Unique, required and sequential combined. */
    private int binaryProperties = BinaryPropertiesInHeuristics.NONE;

    /**
     * If the property is unique, the value is stored here (or null if there isn't any other value).
     * Makes sense only for primitive values.
     */
    private @Nullable Comparable uniqueValue = null;

    /** Either Double, Comparable, or null. */
    private @Nullable Comparable min = null;
    private @Nullable Comparable max = null;

    /** Might be NaN. */
    private double sum = Double.NaN;
    private int count = 1;

    private final BloomFilter bloomFilter = new BloomFilter();
    private final StartingEndingFilter startingEnding = new StartingEndingFilter();

    private PropertyHeuristics(String hierarchicalName) {
        this.hierarchicalName = hierarchicalName;
        setIsUnique(true);
    }

     /**
     * Builds a {@link PropertyHeuristics} object for the given key-value pair.
     * @param key - The hierarchical name to be used for the property.
     * @param value - The value to be analyzed and stored in the heuristics. Only if it's primitive.
     */
    public static PropertyHeuristics createForKeyValuePair(String hierarchicalName, @Nullable Object value) {
        final var output = new PropertyHeuristics(hierarchicalName);

        if (value == null)
            return output;

        output.sum = parseDouble(value);

        output.min = tryParseComparable(value);
        output.max = output.min;

        output.uniqueValue = tryParseUnique(value);

        if (output.uniqueValue != null) {
            output.bloomFilter.add(output.uniqueValue);
            output.startingEnding.add(output.uniqueValue);
        }

        return output;
    }

    public void merge(PropertyHeuristics other) {
        sum += other.sum;
        count += other.getCount();

        if (min != null && other.min != null) {
            if (min instanceof Double thisMin && other.min instanceof Double otherMin) {
                // All mins and maxes are stored as Double.
                min = Math.min(thisMin, otherMin);
                max = Math.max((double) max, (double) other.max);
            }
            else if (min.getClass().equals(other.getMin().getClass())) {
                if (min.compareTo(other.min) > 0)
                    min = other.min;

                if (max.compareTo(other.max) < 0)
                    max = other.max;
            }
        }

        binaryProperties &= other.binaryProperties;
        if (isUnique() && !isUniqueValuesEqual(uniqueValue, other.uniqueValue)) {
            uniqueValue = null;
            setIsUnique(false);
        }

        bloomFilter.merge(other.bloomFilter);
        startingEnding.merge(other.startingEnding);
    }

    /**
     * For sum - we need only things that make sense to sum - basically only numbers.
     */
    private static double parseDouble(Object value) {
        if (value instanceof Number number)
            return number.doubleValue();

        if (value instanceof String string) {
            try {
                return Double.parseDouble(string);
            }
            catch (NumberFormatException e) {
                return Double.NaN;
            }
        }

        return Double.NaN;
    }

    /**
     * For min and max - we can use any comparable.
     * We don't try to convert it to number, because comparables also need a class match for correct comparison.
     */
    private static @Nullable Comparable tryParseComparable(Object value) {
        if (value instanceof Number number)
            return number.doubleValue();
        if (value instanceof Comparable comparable)
            return comparable;

        return null;
    }

    /**
     * We care only about equality.
     * Large objects (BLOBs, CLOBs) are hashed for simpler comparison.
     * If the value can't be easily compared, a null is returned, meaning "unique".
     */
    private static @Nullable Comparable tryParseUnique(Object value) {
        if (value instanceof Number number)
            return number.doubleValue();
        if (value instanceof Comparable comparable)
            return comparable;
        if (value instanceof Blob blob)
            return Hashing.blobToHash(blob);
        if (value instanceof Clob clob)
            return Hashing.clobToHash(clob);

        return null;
    }

    private static boolean isUniqueValuesEqual(Comparable a, Comparable b) {
        return a == null || b == null || a.equals(b);
    }

    public boolean isUnique() {
        return (binaryProperties & BinaryPropertiesInHeuristics.UNIQUE) == BinaryPropertiesInHeuristics.UNIQUE;
    }

    private void setIsUnique(boolean unique) {
        setBinaryProperty(BinaryPropertiesInHeuristics.UNIQUE, unique);
    }

    public boolean isRequired() {
        return (binaryProperties & BinaryPropertiesInHeuristics.REQUIRED) == BinaryPropertiesInHeuristics.REQUIRED;
    }

    public void setIsRequired(boolean required) {
        setBinaryProperty(BinaryPropertiesInHeuristics.REQUIRED, required);
    }

    public boolean isSequential() {
        return (binaryProperties & BinaryPropertiesInHeuristics.SEQUENTIAL) == BinaryPropertiesInHeuristics.SEQUENTIAL;
    }

    public void setIsSequential(boolean sequential) {
        setBinaryProperty(BinaryPropertiesInHeuristics.SEQUENTIAL, sequential);
    }

    private void setBinaryProperty(int flag, boolean value) {
        if (value)
            binaryProperties |= flag;
        else
            binaryProperties &= ~flag;
    }

    public String getHierarchicalName() {
        return hierarchicalName;
    }

    public Object getMin() {
        return min;
    }

    public Object getMax() {
        return max;
    }

    /**
     * If the type T is numeric, the average is a double value derived from the actual numeric values.
     * If T is non-numeric, the average is a double value derived from the hash.
     */
    public double getAverage() {
        return sum / count;
    }

    public double getSum() {
        return sum;
    }

    public int getCount() {
        return count;
    }

    public BloomFilter getBloomFilter() {
        return bloomFilter;
    }

    public StartingEndingFilter getStartingEnding() {
        return startingEnding;
    }

    @Override public String toString() {
        return "PropertyHeuristics{"
            + "unique=" + isUnique()
            + ", required=" + isRequired()
            + ", sequential=" + isSequential()
            + ", hierarchicalName=" + hierarchicalName
            + ", min=" + min
            + ", max=" + max
            + ", average=" + getAverage()
            + ", count=" + count
            + ", bloomFilter=" + bloomFilter
            + ", startingEnding=" + startingEnding
            + '}';
    }
}
