package cz.matfyz.core.adminer;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a document response.
 */
public class DocumentResponse extends DataResponse {
    private List<Map<String, Object>> data;

    public DocumentResponse(List<Map<String, Object>> data, int itemCount, Set<String> propertyNames){
        super(itemCount, propertyNames);
        this.data = data;
    }

    public List<Map<String, Object>> getData() {
        return data;
    }
}
