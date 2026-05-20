package cz.matfyz.wrappermongodb.collector.queryparser;

import cz.matfyz.abstractwrappers.exception.collector.ParseException;
import cz.matfyz.wrappermongodb.collector.MongoDBExceptionsFactory;
import org.bson.Document;

import java.util.List;
import java.util.Set;

/**
 * Class which is responsible for building all of the supported commands from mongosh functions
 */
public class CommandBuilder {

    /**
     * Enum which represents actual return type of last called function, that was used to update command
     */
    public enum ReturnType {
        Collection,
        Cursor,
        None
    }

    /**
     * Field which stores actual command, that is incrementally built during while process
     */
    private final Document command;

    /** Field which stores last returnType */
    private ReturnType returnType;

    /** Field which stores on which mongodb document collection is this command built */
    private final String collectionName;

    public CommandBuilder(String collectionName) {
        this.collectionName = collectionName;
        command = new Document();
        returnType = ReturnType.Collection;
    }

    public Document build() {
        return command;
    }

    /**
     * Updates command by some function.
     * @param function function with which we want to modify our command
     */
    public void updateWithFunction(FunctionItem function) throws ParseException {
        switch (returnType) {
            case Collection:
                updateWithCollectionFunction(function);
                break;
            case Cursor:
                updateWithCursorFunction(function);
                break;
            case None:
                throw new ParseException("You are calling function on unsupported type. You can call specific functions on cursor or collection.");
        }
    }

    private static final Set<String> FIND_NOT_SUPPORTED_OPTIONS = Set.of("explain", "maxAwaitTimeMS", "readPreference");

    /**
     * Updates actual command with options object represented as document.
     * @param options instance of Document which represents the inputted options
     */
    private void updateWithOptions(String functionName, Document options) throws ParseException {
        for (var entry : options.entrySet()) {
            if ("find".equals(functionName) && FIND_NOT_SUPPORTED_OPTIONS.contains(entry.getKey()))
                throw MongoDBExceptionsFactory.getExceptionsFactory().invalidMethodOption("explain", "find", returnType);
            command.put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Updates actual command by some document value parsed from arguments of functions.
     * @param keyName name which will be used as a key name in document command
     * @param documentValue instance of document to be set as value
     */
    private void updateWithDocumentValue(String keyName, Document documentValue) {
        if (!documentValue.isEmpty())
            command.put(keyName, documentValue);
    }

    /**
     * Updates actual command by some document list value.
     * @param keyName name which will be used as a key name in document command
     * @param arrayDocValue instance of list to be used as newly added value
     */
    private void updateWithDocumentListValue(String keyName, List<Document> arrayDocValue) {
        if (!arrayDocValue.isEmpty())
            command.put(keyName, arrayDocValue);
    }

    /**
     * Called on Collection type for updating actual command as a count.
     * Is not used, because after some testing I figured out that result and explain structure is too different from find command.
     */
    private void updateWithCollectionCount(FunctionItem function) throws ParseException {
        command.put("count", collectionName);
        switch (function.args.size()) {
            case 0:
                break;
            case 1:
                updateWithDocumentValue("query", function.args.getDocument(0));
                break;
            case 2:
                updateWithDocumentValue("query", function.args.getDocument(0));
                updateWithOptions(function.name, function.args.getDocument(1));
                break;
            default:
                throw MongoDBExceptionsFactory.getExceptionsFactory().invalidNumberOfArgumentsInMethod(function.name, returnType);
        }
    }

    /**
     * Called on Collection type for updating actual command as an aggregate.
     * Is not used, because after some testing I figured out that result and explain structure is too different from find command.
     */
    private void updateWithCollectionAggregate(FunctionItem function) throws ParseException {
        command.put("aggregate", collectionName);
        command.put("cursor", new Document());
        switch (function.args.size()) {
            case 0:
                break;
            case 1:
                updateWithDocumentListValue("pipeline", function.args.getDocumentList(0));
                break;
            case 2:
                updateWithDocumentListValue("pipeline", function.args.getDocumentList(0));
                updateWithOptions(function.name, function.args.getDocument(1));
                break;
            default:
                throw MongoDBExceptionsFactory.getExceptionsFactory().invalidNumberOfArgumentsInMethod(function.name, returnType);
        }
        returnType = ReturnType.Cursor;
    }

    private void updateWithCollectionFind(FunctionItem function) throws ParseException {
        command.put("find", collectionName);
        switch (function.args.size()) {
            case 0:
                break;
            case 1:
                updateWithDocumentValue("filter", function.args.getDocument(0));
                break;
            case 2:
                updateWithDocumentValue("filter", function.args.getDocument(0));
                updateWithDocumentValue("projection", function.args.getDocument(1));
                break;
            case 3:
                updateWithDocumentValue("filter", function.args.getDocument(0));
                updateWithDocumentValue("projection", function.args.getDocument(1));
                updateWithOptions(function.name, function.args.getDocument(2));
                break;
            default:
                throw MongoDBExceptionsFactory.getExceptionsFactory().invalidNumberOfArgumentsInMethod(function.name, returnType);
        }
        returnType = ReturnType.Cursor;
    }

    /**
     * Called on Collection type for updating actual command as a distinct.
     * Is not used, because after some testing I figured out that result and explain structure is too different from find command.
     */
    private void updateWithCollectionDistinct(FunctionItem function) throws ParseException {
        command.put("distinct", collectionName);

        switch (function.args.size()) {
            case 0:
                break;
            case 1:
                command.put("key", function.args.getString(0));
                break;
            case 2:
                command.put("key", function.args.getString(0));
                updateWithDocumentValue("query", function.args.getDocument(1));
                break;
            case 3:
                command.put("key", function.args.getString(0));
                updateWithDocumentValue("query", function.args.getDocument(1));
                updateWithOptions(function.name, function.args.getDocument(2));
                break;
            default:
                throw MongoDBExceptionsFactory.getExceptionsFactory().invalidNumberOfArgumentsInMethod(function.name, returnType);
        }

        returnType = ReturnType.None;
    }

    private void updateWithCollectionFunction(FunctionItem function) throws ParseException {
        if ("find".equals(function.name))
            updateWithCollectionFind(function);
        else if ("aggregate".equals(function.name) || "count".equals(function.name) || "distinct".equals(function.name))
            throw MongoDBExceptionsFactory.getExceptionsFactory().notSupportedMethod("aggregate", returnType);
        else
            throw MongoDBExceptionsFactory.getExceptionsFactory().invalidMethod(function.name, returnType);
    }

    private void updateWithCursorFunction(FunctionItem function) throws ParseException {
        switch(function.name) {
            case "allowDiskUse", "allowPartialResults", "noCursorTimeout", "returnKey", "showRecordId", "tailable":
                updateWithCursorFlagMethod(function);
                break;
            case "batchSize", "limit", "maxTimeMS", "skip":
                updateWithCursorIntegerMethod(function);
                break;
            case "collation", "max", "min", "sort":
                updateWithCursorDocumentMethod(function);
                break;
            case "comment", "readConcern":
                updateWithCursorStringMethod(function);
                break;
            case "hint":
                updateWithCursorHint(function);
                break;
            case "count":
                throw MongoDBExceptionsFactory.getExceptionsFactory().notSupportedMethod("count", returnType);
            default:
                throw MongoDBExceptionsFactory.getExceptionsFactory().invalidMethod(function.name, returnType);
        }
    }

    private void updateWithCursorFlagMethod(FunctionItem function) throws ParseException {
        if (function.args.size() == 0)
            command.put(function.name, true);
        else if (function.args.size() == 1)
            command.put(function.name, function.args.getBoolean(0));
        else
            throw MongoDBExceptionsFactory.getExceptionsFactory().invalidNumberOfArgumentsInMethod(function.name, returnType);
        returnType = ReturnType.Cursor;
    }

    private void updateWithCursorIntegerMethod(FunctionItem function) throws ParseException {
        if (function.args.size() == 1)
            command.put(function.name, function.args.getInteger(0));
        else
            throw MongoDBExceptionsFactory.getExceptionsFactory().invalidNumberOfArgumentsInMethod(function.name, returnType);
        returnType = ReturnType.Cursor;
    }

    private void updateWithCursorDocumentMethod(FunctionItem function) throws ParseException {
        if (function.args.size() == 1)
            command.put(function.name, function.args.getDocument(0));
        else
            throw MongoDBExceptionsFactory.getExceptionsFactory().invalidNumberOfArgumentsInMethod(function.name, returnType);
        returnType = ReturnType.Cursor;
    }

    private void updateWithCursorStringMethod(FunctionItem function) throws ParseException {
        if (function.args.size() == 1)
            command.put(function.name, function.args.getString(0));
        else
            throw MongoDBExceptionsFactory.getExceptionsFactory().invalidNumberOfArgumentsInMethod(function.name, returnType);
        returnType = ReturnType.Cursor;
    }

    private void updateWithCursorHint(FunctionItem function) throws ParseException {
        if (function.args.size() == 1) {
            try {
                Document hintDocument = function.args.getDocument(0);
                command.put("hint", hintDocument);
            } catch (ParseException e) {
                command.put("hint", function.args.getString(0));
            }
        } else {
            throw MongoDBExceptionsFactory.getExceptionsFactory().invalidNumberOfArgumentsInMethod(function.name, returnType);
        }
        returnType = ReturnType.Cursor;
    }

    /** Not used because count produce different format of result and explain tree. */
    private void updateWithCursorCount(FunctionItem function) throws ParseException {
        if (function.args.size() == 0) {
            if (command.containsKey("find")) {
                command.remove("find");
                command.put("count", collectionName);
                if (command.containsKey("filter")) {
                    Document filterDoc = command.get("filter", Document.class);
                    command.remove("filter");
                    command.put("query", filterDoc);
                }
            } else {
                throw MongoDBExceptionsFactory.getExceptionsFactory().invalidCountUsage();
            }

        } else {
            throw MongoDBExceptionsFactory.getExceptionsFactory().invalidNumberOfArgumentsInMethod(function.name, returnType);
        }
        returnType = ReturnType.None;
    }

}
