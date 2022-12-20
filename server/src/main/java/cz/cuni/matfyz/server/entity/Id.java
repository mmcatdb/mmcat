package cz.cuni.matfyz.server.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.lang.NonNull;

@JsonSerialize(using = IdSerializer.class)
@JsonDeserialize(using = IdDeserializer.class)
public class Id implements java.io.Serializable, java.lang.Comparable<Id>, java.lang.CharSequence {

    @NonNull
    public final String value;

    public Id(String value) {
        this.value = value;
    }
    
    @Override
    public String toString() {
        return value;
    }

    @Override
    public char charAt(int index) {
        return value.charAt(index);
    }

    @Override
    public int length() {
        return value.length();
    }

    @Override
    public CharSequence subSequence(int beginIndex, int endIndex) {
        return value.subSequence(beginIndex, endIndex);
    }

    public int compareTo(Id another) {
        return value.compareTo(another.value);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof Id another && another != null && value.equals(another.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
