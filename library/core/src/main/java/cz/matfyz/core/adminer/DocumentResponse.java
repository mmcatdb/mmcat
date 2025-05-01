package cz.matfyz.core.adminer;

import java.util.List;
import java.util.Set;

import org.bson.Document;

/**
 * Represents a document response.
 */
public class DocumentResponse extends DataResponse {
    private List<Document> data;

    public DocumentResponse(List<Document> data, int itemCount, Set<String> propertyNames){
        super(itemCount, propertyNames);
        this.data = data;
    }

    public List<Document> getData() {
        return data;
    }
}
