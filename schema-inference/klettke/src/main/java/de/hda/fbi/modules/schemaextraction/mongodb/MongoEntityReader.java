package de.hda.fbi.modules.schemaextraction.mongodb;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientOptions.Builder;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import de.hda.fbi.modules.schemaextraction.EntityReader;
import de.hda.fbi.modules.schemaextraction.common.EntityInfo;
import de.hda.fbi.modules.schemaextraction.configuration.DatabaseConfiguration;
import de.hda.fbi.modules.schemaextraction.configuration.SchemaExtractionConfiguration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import de.hda.fbi.modules.schemaextraction.impl.ExtractionFacadeImpl;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MongoEntityReader implements EntityReader {

//    private static Logger LOGGER = LoggerFactory.getLogger(MongoEntityReader.class);

    private MongoClient mongoClient;

    public List<EntityInfo> readEntitiesSorted(SchemaExtractionConfiguration extractionConfiguration, List<String> entityName) {
//        LOGGER.debug("Start reading entity type {}!", entityName);
        JsonParser parser = new JsonParser();
        List<FutureTask<List<EntityInfo>>> taskList = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(entityName.size());
        List<EntityInfo> documents = new ArrayList<EntityInfo>();

        String timestampIdentifier = extractionConfiguration.getTimestampIdentifier();
        Object lastExtractedTimestmap = extractionConfiguration.getLastExtractedTimestamp();

        MongoDatabase database = this.getMongoClient(extractionConfiguration.getDatabaseConfiguration())
                .getDatabase(extractionConfiguration.getDatabaseName());

        for (String entityType : entityName) {
//            LOGGER.debug("Start adding entity infos for entity type {}!", entityType);
            FutureTask<List<EntityInfo>> task = new FutureTask<>(new ReadEntitiesSortedCallable(database, entityType, timestampIdentifier, lastExtractedTimestmap));
            executor.execute(task);
            taskList.add(task);
        }

        for (FutureTask<List<EntityInfo>> task : taskList) {
            try {
                documents.addAll(task.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        // Liste sortieren
        Collections.sort(documents, new Comparator<EntityInfo>() {
            public int compare(EntityInfo document1, EntityInfo document2) {
                return document1.getTimestamp().compareTo(document2.getTimestamp());
            }
        });

		System.out.println("VELIKOST MONGO_ENTITY_READER: " + documents.size());
        return documents;
    }

    private MongoClient getMongoClient(DatabaseConfiguration databaseConfiguration) {

        if (mongoClient == null) {
            if (databaseConfiguration.isAuth()) {

                Codec<Document> defaultJsonArrayCodec = MongoClient.getDefaultCodecRegistry().get(Document.class);

                CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(),
                        CodecRegistries.fromCodecs(defaultJsonArrayCodec));

                MongoCredential credential = MongoCredential.createCredential(databaseConfiguration.getUserName(),
                        databaseConfiguration.getAuthDbName(), databaseConfiguration.getPassword().toCharArray());
                Builder builder = MongoClientOptions.builder();

                builder.codecRegistry(codecRegistry);

                // builder.sslEnabled(true);
                this.mongoClient = new MongoClient(
                        new ServerAddress(databaseConfiguration.getHost(),
                                Integer.parseInt(databaseConfiguration.getPort())),
                        Arrays.asList(credential), builder.build());

            } else {
                mongoClient = new MongoClient(new ServerAddress(databaseConfiguration.getHost(),
                        Integer.parseInt(databaseConfiguration.getPort())));
            }
        }
        return this.mongoClient;
    }
}
