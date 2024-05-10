package cz.matfyz.core.rsd.utils;

public class BasicHashFunction implements HashFunction {

    @Override
    public Integer apply(Object value) {
        return Math.abs(value.toString().hashCode() % 10);
    }

}
