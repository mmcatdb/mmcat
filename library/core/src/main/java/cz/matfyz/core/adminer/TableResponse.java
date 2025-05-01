package cz.matfyz.core.adminer;

import java.util.List;

/**
 * Represents a table response.
 */
public class TableResponse extends DataResponse {
    private List<List<String>> data;

    public TableResponse(List<List<String>> data, int itemCount, List<String> propertyNames){
        super(itemCount, propertyNames);
        this.data = data;
    }

    public List<List<String>> getData() {
        return data;
    }
}
