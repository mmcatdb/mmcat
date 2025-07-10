package cz.matfyz.core.adminer;

import java.util.List;

import org.bson.Document;

/**
 * Represents a document response.
 */
public class DocumentResponse extends DataResponse {

    private List<Document> data;
    private static final String TYPE = "document";

    public DocumentResponse(List<Document> data, long itemCount, List<String> propertyNames) {
        super(itemCount, propertyNames);
        this.data = data;
    }

    public List<Document> getData() {
        return data;
    }

    @Override public String getType() {
        return TYPE;
    }

}
