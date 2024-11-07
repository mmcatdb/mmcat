package cz.matfyz.core.rsd;

import cz.matfyz.core.rsd.utils.BasicHashFunction;
import cz.matfyz.core.rsd.utils.BloomFilter;
import cz.matfyz.core.rsd.utils.StartingEndingFilter;
import java.io.Serializable;

public class PropertyHeuristics implements Serializable {

    // boolean unique;

    // boolean required;    // pokud narazis na null hodnotu, je to jasne... ale pokud ta hodnota neexistuje, tak se o ni nedozvis jinak nez porovnanim poctu recordu a poctu vyskytu te property
    // a totez unique ziskas porovnanim

    // boolean sequential;

    // unique, required and sequential combined
    int binaryProperties = BinaryPropertiesInHeuristics.NONE;

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
    public String hierarchicalName;

    private Object min;

    private Object max;

    private double average;    // pokud je T numericke, pak prumer je desetinne cislo ziskane z hodnot
    // pokud T neni numericke, pak je prumerem deetinne cislo ziskane z hashu

    private double temp;    //

    private int count;    // pro inkrementaci, aby se na konci mohlo spocitat average jako temp/count

    private int first;

    //private int parentCount;        //kolikrat se vyskytuje primy predek v scheme

    private BloomFilter bloomFilter;

        private StartingEndingFilter startingEnding;

    public PropertyHeuristics() {
        average = 0.0;
        temp = 0.0;
        count = 0;
        //parentCount = 0;
        bloomFilter = new BloomFilter();
                startingEnding = new StartingEndingFilter();
    }

        private void setBinaryProperty(int flag, boolean value) {
            if (value) {
                binaryProperties |= flag;
            } else {
                binaryProperties &= ~flag;
            }
        }

    public boolean isUnique() {
        return (binaryProperties & BinaryPropertiesInHeuristics.UNIQUE) == BinaryPropertiesInHeuristics.UNIQUE;
    }

    public void setUnique(boolean unique) {
        setBinaryProperty(BinaryPropertiesInHeuristics.UNIQUE, unique);
    }

    public boolean isRequired() {
        return (binaryProperties & BinaryPropertiesInHeuristics.REQUIRED) == BinaryPropertiesInHeuristics.REQUIRED;
    }

    public void setRequired(boolean required) {
        setBinaryProperty(BinaryPropertiesInHeuristics.REQUIRED, required);
    }

    public boolean isSequential() {
        return (binaryProperties & BinaryPropertiesInHeuristics.SEQUENTIAL) == BinaryPropertiesInHeuristics.SEQUENTIAL;
    }

    public void setSequential(boolean sequential) {
        setBinaryProperty(BinaryPropertiesInHeuristics.SEQUENTIAL, sequential);
    }

    public String getHierarchicalName() {
        return hierarchicalName;
    }

    public void setHierarchicalName(String hierarchicalName) {
        this.hierarchicalName = hierarchicalName;
    }

    public Object getMin() {
        return min;
    }

    public void setMin(Object min) {
        this.min = min;
    }

    public Object getMax() {
        return max;
    }

    public void setMax(Object max) {
        this.max = max;
    }

    public double getAverage() {
        return average;
    }

    public void setAverage(double average) {
        this.average = average;
    }

    public double getTemp() {
        return temp;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getFirst() {
        return first;
    }

    public void setFirst(int first) {
        this.first = first;
    }

    public BloomFilter getBloomFilter() {
        return bloomFilter;
    }

    public void setBloomFilter(BloomFilter bloomFilter) {
        this.bloomFilter = bloomFilter;
    }

    // public int getParentCount() {
    //    return parentCount;
    // }

    // public void setParentCount(int parentCount) {
    //    this.parentCount = parentCount;
    // }

        public void addToStartingEnding(Object value) {
            this.startingEnding.add(value);
        }

    public void add(Object value) {
        if (value instanceof Number && min instanceof Number) {
            this.addNumber((Number) min, (Number) max, (Number) value);
        } else if ((value instanceof Comparable && min instanceof Comparable) && (value.getClass().equals(min.getClass()))) {
            this.addComparable((Comparable) min, (Comparable) max, (Comparable) value);
        } else if (value != null) {
            this.addComparable(min.toString(), max.toString(), value.toString());
        }

        if (value != null) {
            bloomFilter.add(value);
        }
    }

    private void addComparable(Comparable _min, Comparable _max, Comparable _value) {
        double resultOfHashFunction = new BasicHashFunction().apply(_value).doubleValue();

        if (_min.compareTo(_value) > 0) {
            min = _value;
        } else if (_max.compareTo(_value) < 0) {
            max = _value;
        }

        temp += resultOfHashFunction;
        ++count;
    }

    private void addNumber(Number _min, Number _max, Number _value) {
        double __min = _min.doubleValue();
        double __max = _max.doubleValue();
        double __value = _value.doubleValue();
        if (__min > __value) {
            min = _value;
        } else if (__max > __value) {
            max = _value;
        }

        temp += __value;
        ++count;
    }

    public void merge(PropertyHeuristics heuristics) {
        if (heuristics.min != null) {
            if (heuristics.min instanceof Number && min instanceof Number && heuristics.max instanceof Number && max instanceof Number) {
                double _min = ((Number) min).doubleValue();
                double _max = ((Number) max).doubleValue();
                double __min = ((Number) heuristics.getMin()).doubleValue();
                double __max = ((Number) heuristics.getMax()).doubleValue();
                if (_min > (__min)) {
                    min = __min;
                }
                if (_max < __max) {
                    max = __max;
                }
                temp += ((Number) heuristics.getTemp()).doubleValue();
            } else if ((heuristics.min instanceof Comparable && min instanceof Comparable && heuristics.max instanceof Comparable && max instanceof Comparable) &&
                    (heuristics.min.getClass().equals(min.getClass()) && heuristics.max.getClass().equals(max.getClass()))) {
                Comparable _min = (Comparable) min;
                Comparable _max = (Comparable) max;
                Comparable __min = (Comparable) heuristics.min;
                Comparable __max = (Comparable) heuristics.max;
                if (_min.compareTo(__min) > 0) {
                    min = __min;
                }
                if (_max == null || _max.compareTo(__max) < 0) {
                    max = __max;
                }
                temp += ((Number) heuristics.getTemp()).doubleValue();
            } else {
                String __min = heuristics.getMin().toString();
                String __max = heuristics.getMax().toString();
                if (min == null || min.toString().compareTo(__min) > 0) {
                    min = __min;
                }
                if (max == null || max.toString().compareTo(__max) < 0) {
                    max = __max;
                }
                temp += ((Number) heuristics.getTemp()).doubleValue();
            }
        }

        count += heuristics.getCount();
        first += heuristics.getFirst();

        // unique = unique && heuristics.unique;
        // required = required && heuristics.required;
        // sequential = sequential && heuristics.sequential;
                binaryProperties &= heuristics.binaryProperties;

                // spojování starting a ending počtů
                startingEnding.merge(heuristics.startingEnding);

        bloomFilter.merge(heuristics.getBloomFilter());
    }

    @Override public String toString() {
        return "PropertyHeuristics{"
            + "unique=" + isUnique()
            + ", required=" + isRequired()
            + ", sequential=" + isSequential()
            + ", hierarchicalName=" + hierarchicalName
            + ", min=" + min
            + ", max=" + max
            + ", average=" + average
        //    + ", temp=" + temp
            + ", count=" + count
            +  ", first=" + first
            // +  ", parentCount=" + parentCount
        //    + ", bloomFilter=" + bloomFilter
        //    + " " + startingEnding.toString()
            + '}';
    }
}
