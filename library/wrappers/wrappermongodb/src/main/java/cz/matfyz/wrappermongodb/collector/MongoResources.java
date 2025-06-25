package cz.matfyz.wrappermongodb.collector;

import org.bson.Document;

import java.util.LinkedList;
import java.util.List;

/**
 * Class containing all help queries used for gather data and some constants for computing sizes etc.
 */
public class MongoResources {
    public static final String SYSTEM_NAME = "MongoDB";

    public static Document getExplainCommand(Document command) {
        if (command.containsKey("aggregate")) {
            command.put("explain", true);
            return command;
        } else {
            Document newCommand = new Document();
            newCommand.put("explain", command);
            newCommand.put("verbosity", "executionStats");
            return newCommand;
        }
    }
    public static Document getCollectionStatsCommand(String collectionName) {
        return new Document("collStats", collectionName);
    }
    public static Document getIndexRowCountCommand(String collectionName, String indexName) {
        Document countCommand = new Document();
        countCommand.put("count", collectionName);
        countCommand.put("hint", indexName);
        return countCommand;
    }

    public static Document getCollectionInfoCommand(String collectionName) {
        Document command = new Document();
        command.put("listCollections", 1);
        command.put("filter", new Document("name", collectionName));
        return command;
    }

    public static Document getFieldsInCollectionCommand(String collectionName) {
        Document command = new Document();
        command.put("aggregate", collectionName);
        command.put("cursor", new Document());

        List<Document> pipeline = new LinkedList<>();
        pipeline.add(new Document("$project", new Document("arrayOfKeyValue", new Document("$objectToArray", "$$ROOT"))));
        pipeline.add(new Document("$unwind", "$arrayOfKeyValue"));

        Document groupCommand = new Document();
        groupCommand.put("_id", null);
        groupCommand.put("allKeys", new Document("$addToSet", "$arrayOfKeyValue.k"));
        pipeline.add(new Document("$group", groupCommand));

        command.put("pipeline", pipeline);

        return command;
    }

    public static Document getFieldTypeCommand(String collectionName, String fieldName) {
        Document command = new Document();
        command.put("aggregate", collectionName);
        command.put("cursor", new Document());

        List<Document> pipeline = new LinkedList<>();
        Document projectCommand = new Document();
        projectCommand.put("fieldType", new Document("$type", '$' + fieldName));
        projectCommand.put("_id", 0);
        pipeline.add(new Document("$project", projectCommand));

        Document groupCommand = new Document();
        groupCommand.put("_id", new Document("fieldType", "$fieldType"));
        groupCommand.put("count", new Document("$sum", 1));
        pipeline.add(new Document("$group", groupCommand));

        command.put("pipeline", pipeline);

        return command;
    }
    public static Document getAvgObjectStringSizeCommand(String collectionName, String fieldName, String type) {
        Document command = new Document();
        command.put("aggregate", collectionName);
        command.put("cursor", new Document());

        String typeOper = ("object".equals(type)) ? "$bsonSize" : "$binarySize";

        List<Document> pipeline = new LinkedList<>();
        pipeline.add(new Document("$match", new Document(fieldName, new Document("$type", type))));
        pipeline.add(new Document("$project", new Document("size", new Document(typeOper, '$' + fieldName))));

        Document groupDoc = new Document();
        groupDoc.put("_id", null);
        groupDoc.put("avg", new Document("$avg", "$size"));
        pipeline.add(new Document("$group", groupDoc));

        command.put("pipeline", pipeline);
        return command;
    }

    public static Document getNextBatchOfCursorCommand(long cursor, String collectionName) {
        Document command = new Document();
        command.put("getMore", cursor);
        command.put("collection", collectionName);
        return command;
    }
    public static Document getDatasetStatsCommand() {
        return new Document("dbStats", 1);
    }

    public static Document getServerStatsCommand() {
        return new Document("serverStatus", 1);
    }

    public static String getConnectionLink(String host, int port, String user, String password) {
        if (user.isEmpty() || password.isEmpty())
            return "mongodb://" + host + ':' + port;
        else
            return "mongodb://" + user + ':' + password + '@' + host + ':' + port;
    }

    public static class DefaultSizes {
        public static Integer getAvgColumnSizeByType(String type) {
            if ("int".equals(type))
                return 4;
            else if ("objectId".equals(type))
                return 12;
            else if ("bool".equals(type))
                return 1;
            else if ("date".equals(type) || "long".equals(type) || "double".equals(type))
                return 8;
            else if ("decimal".equals(type))
                return 16;
            else
                return  null;
        }

        public static int PAGE_SIZE = 4096;
    }
}
