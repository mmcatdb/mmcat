package cz.matfyz.core.collector;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ColumnType {
    @JsonProperty("byteSize")
    private Integer _byteSize;

    @JsonProperty("ratio")
    private Double _ratio;

    public ColumnType() {
        _byteSize = null;
        _ratio = null;
    }

    @JsonIgnore
    public int getByteSize() { return _byteSize; }

    public void setByteSize(int size) {
        if (_byteSize == null)
            _byteSize = size;
    }

    public void setRatio(double ratio) {
        if (_ratio == null)
            _ratio = ratio;
    }
}
