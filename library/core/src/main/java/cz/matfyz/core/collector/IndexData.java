package cz.matfyz.core.collector;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Class for saving statistical data about index
 */
public class IndexData {
    /** Field holding index size in bytes */
    @JsonProperty("byteSize")
    public Long _size;
    /** Field holding index size in pages */
    @JsonProperty("sizeInPages")
    public Long _sizeInPages;
    /** Field holding index row count */
    @JsonProperty("rowCount")
    public Long _rowCount;

    public IndexData() {
        _size = null;
        _sizeInPages = null;
        _rowCount = null;
    }

    public void setByteSize(long size) {
        if (_size == null) {_size = size;}
    }

    public void setSizeInPages(long sizeInPages) {
        if (_sizeInPages == null) {_sizeInPages = sizeInPages;}
    }

    public void setRowCount(long count) {
        if (_rowCount == null) {_rowCount = count;}
    }
}
