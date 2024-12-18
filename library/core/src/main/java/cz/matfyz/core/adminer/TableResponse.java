package cz.matfyz.core.adminer;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a table response.
 */
public class TableResponse extends DataResponse {
    private List<Map<String, String>> data;

    public TableResponse(List<Map<String, String>> data, int itemCount, Set<String> propertyNames){
        super(itemCount, propertyNames);
        this.data = data;
    }

    public List<Map<String, String>> getData() {
        return data;
    }
}
