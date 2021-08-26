package de.hda.fbi.modules.schemaextraction.mongodb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;

import org.bson.Document;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import de.hda.fbi.modules.schemaextraction.common.EntityInfo;

public class ReadEntitiesSortedCallable implements Callable<List<EntityInfo>> {

    private final MongoDatabase DATABASE;
    private final String ENTITY_NAME;
    private final String TIMESTAMP_IDENTIFIER;
    private final Object LAST_EXTRACTED_TIMESTAMP;

    public ReadEntitiesSortedCallable(MongoDatabase database, String entityName, String timestampIdentifier, Object lastExtractedTimestmap) {
        this.DATABASE = database;
        this.ENTITY_NAME = entityName;
        this.TIMESTAMP_IDENTIFIER = timestampIdentifier;
        this.LAST_EXTRACTED_TIMESTAMP = lastExtractedTimestmap;
    }

    @Override
    public List<EntityInfo> call() throws Exception {
        JsonParser parser = new JsonParser();
        List<EntityInfo> documents = new ArrayList<EntityInfo>();

        MongoCursor<Document> documentIterator;
        if (LAST_EXTRACTED_TIMESTAMP != null) {
            documentIterator = DATABASE.getCollection(ENTITY_NAME)
                    .find(Filters.gt(TIMESTAMP_IDENTIFIER, LAST_EXTRACTED_TIMESTAMP)).batchSize(1000).iterator();
        } else {
            documentIterator = DATABASE.getCollection(ENTITY_NAME).find().batchSize(1000).iterator();
        }

        // über alle Dokumente iterieren, Id auslesen und Zeitpunkt der
        // Dokumentenerstellung auslesen

        while (documentIterator.hasNext()) {
            Document doc = documentIterator.next();
            Object id = doc.getObjectId("_id").toHexString();

            JsonObject node = parser.parse(doc.toJson()).getAsJsonObject();

            if (node.has(TIMESTAMP_IDENTIFIER)) {
                int timestamp = node.get(TIMESTAMP_IDENTIFIER).getAsInt();
                EntityInfo documentInfo = new EntityInfo(node, timestamp, ENTITY_NAME, id);
                // node.remove("timestamp");
                documents.add(documentInfo);
            }
        }
        documentIterator.close();
        return documents;
    }

}
