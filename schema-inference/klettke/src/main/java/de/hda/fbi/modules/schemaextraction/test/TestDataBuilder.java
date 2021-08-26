package de.hda.fbi.modules.schemaextraction.test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.DBCollection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import de.hda.fbi.modules.schemaextraction.common.EntityInfo;
import de.hda.fbi.modules.schemaextraction.common.GlobalTimestampProvider;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

public class TestDataBuilder {

    private Document document;

    private int number = 1;

    private String collectionName;

    private MongoDatabase database;

    private boolean persistInDatabase;

    private java.util.List<EntityInfo> documentInfos = new ArrayList<EntityInfo>();

    public TestDataBuilder(MongoDatabase database) {
        this.database = database;
        this.persistInDatabase = true;
        GlobalTimestampProvider.getInstance().clear();
    }

    public TestDataBuilder() {
        this.persistInDatabase = false;
        GlobalTimestampProvider.getInstance().clear();
    }

    private void CreateIfNull() {
        if (document == null) {
            this.document = new Document();
        }
    }

    public TestDataBuilder ForCollectionName(String collectionName) {
        this.CreateIfNull();
        this.collectionName = collectionName;
        return this;
    }

    public TestDataBuilder With(String propertyName, Object value) {

        this.CreateIfNull();
        this.document.append(propertyName, value);
        return this;
    }

    public void BuildAndPersist() {

        MongoCollection<Document> collection = null;
        if (persistInDatabase) {
            collection = this.database.getCollection(this.collectionName);
        }

        for (int i = 0; i < this.number; i++) {

            this.document.append(DBCollection.ID_FIELD_NAME, new ObjectId());

            if (persistInDatabase) {
                collection.insertOne(new Document(this.document));
            } else {
                if (!this.document.containsKey("ts")) {
                    this.document.append("ts", GlobalTimestampProvider.getInstance().getNextTimestamp());
                }
                Integer ts = this.document.getInteger("ts");
                JsonObject jsonObject = new JsonParser().parse(new Document(this.document).toJson()).getAsJsonObject();

                EntityInfo docInfo = new EntityInfo(jsonObject, ts, this.collectionName,
                        this.document.getObjectId(DBCollection.ID_FIELD_NAME).toHexString());
                this.documentInfos.add(docInfo);
            }
        }

        this.document = null;
    }

    public List<EntityInfo> getDocumentInfos() {
        return documentInfos;
    }
}
