package cz.matfyz.wrappermongodb.collector.queryparser;

import cz.matfyz.abstractwrappers.exception.collector.ParseException;
import cz.matfyz.wrappermongodb.collector.MongoExceptionsFactory;
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
    private ReturnType _returnType;

    /** Field which stores on which mongodb document collection is this command built */
    private final String collectionName;

    private final MongoExceptionsFactory exceptionsFactory;

    public CommandBuilder(String collectionName, MongoExceptionsFactory exceptionsFactory) {
        this.collectionName = collectionName;
        command = new Document();
        _returnType = ReturnType.Collection;
        this.exceptionsFactory = exceptionsFactory;
    }

    /**
     * Builder method for building final database command as Document
     * @return the final command as Document
     */
    public Document build() {
        return command;
    }

    /**
     * Private static field which contains name of options, which are not supported to use with our find command
     */
    private static final Set<String> FIND_NOT_SUPPORTED_OPTIONS = Set.of("explain", "maxAwaitTimeMS", "readPreference");

    /**
     * Method which will update actual command with options object represented as document
     * @param functionName name of function by which is command updated
     * @param options instance of Document which represents the inputted options
     * @throws ParseException when invalid option is used in options object which we do not support
     */
    private void updateWithOptions(String functionName, Document options) throws ParseException {
        for (var entry : options.entrySet()) {

            if ("find".equals(functionName) && FIND_NOT_SUPPORTED_OPTIONS.contains(entry.getKey()))
                throw exceptionsFactory.invalidMethodOption("explain", "find", _returnType);
            command.put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Method which will update actual command by some document value parsed from arguments of functions
     * @param keyName name which will be used as a key name in document command
     * @param documentValue instance of document to be set as value
     */
    private void updateWithDocumentValue(String keyName, Document documentValue) {
        if (!documentValue.isEmpty())
            command.put(keyName, documentValue);
    }

    /**
     * Method which will update actual command by some document list value
     * @param keyName name which will be used as a key name in document command
     * @param arrayDocValue instance of list to be used as newly added value
     */
    private void updateWithDocumentListValue(String keyName, List<Document> arrayDocValue) {
        if (!arrayDocValue.isEmpty())
            command.put(keyName, arrayDocValue);
    }

    /**
     * Method called on Collection type for updating actual command as a count.
     * Is not used, because after some testing I figured out that result and explain structure is too different from find command.
     * @param function inputted function to update command
     * @throws ParseException when function is called with incorrect count of parameters or some values cannot be parsed
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
                throw exceptionsFactory.invalidNumberOfArgumentsInMethod(function.name, _returnType);
        }
    }

    /**
     * Method called on Collection type for updating actual command as an aggregate.
     * Is not used, because after some testing I figured out that result and explain structure is too different from find command.
     * @param function inputted function to update command
     * @throws ParseException when function is called with incorrect count of parameters or some values cannot be parsed
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
                throw exceptionsFactory.invalidNumberOfArgumentsInMethod(function.name, _returnType);
        }
        _returnType = ReturnType.Cursor;
    }

    /**
     * Method for updating actual command as a find command.
     * @param function inputted function to update command
     * @throws ParseException when function is called with incorrect count of parameters or some values cannot be parsed
     */
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
                throw exceptionsFactory.invalidNumberOfArgumentsInMethod(function.name, _returnType);
        }
        _returnType = ReturnType.Cursor;
    }

    /**
     * Method called on Collection type for updating actual command as a distinct.
     * Is not used, because after some testing I figured out that result and explain structure is too different from find command.
     * @param function inputted function to update command
     * @throws ParseException when function is called with incorrect count of parameters or some values cannot be parsed
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
                throw exceptionsFactory.invalidNumberOfArgumentsInMethod(function.name, _returnType);
        }

        _returnType = ReturnType.None;
    }

    /**
     * Method which takes a collection function to update command
     * and decide which of the supported functions to use based on functions name
     * @param function inputted function
     * @throws ParseException when function of nonexistent name or function our system do not support was used
     */
    private void updateWithCollectionFunction(FunctionItem function) throws ParseException {
        if ("find".equals(function.name))
            updateWithCollectionFind(function);
        else if ("aggregate".equals(function.name) || "count".equals(function.name) || "distinct".equals(function.name))
            throw exceptionsFactory.notSupportedMethod("aggregate", _returnType);
        else
            throw exceptionsFactory.invalidMethod(function.name, _returnType);
    }

    /**
     * Public method which is used to update our command by some function
     * @param function function with which we want to modify our command
     * @throws ParseException throws when function does not exist or cannot be called on lastly returned type or some problems with arguments occur
     */
    public void updateWithFunction(FunctionItem function) throws ParseException {
        switch (_returnType) {
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

    /**
     * Method to update our command with some flag function that was called on cursor
     * @param function inputted function
     * @throws ParseException when function has invalid number of arguments
     */
    private void updateWithCursorFlagMethod(FunctionItem function) throws ParseException {
        if (function.args.size() == 0)
            command.put(function.name, true);
        else if (function.args.size() == 1)
            command.put(function.name, function.args.getBoolean(0));
        else
            throw exceptionsFactory.invalidNumberOfArgumentsInMethod(function.name, _returnType);
        _returnType = ReturnType.Cursor;
    }

    /**
     * Method for updating command by function which argument should be integer
     * @param function inputted function
     * @throws ParseException when function is called with invalid number of arguments, or some of the arguments couldn't be parsed
     */
    private void updateWithCursorIntegerMethod(FunctionItem function) throws ParseException {
        if (function.args.size() == 1)
            command.put(function.name, function.args.getInteger(0));
        else
            throw exceptionsFactory.invalidNumberOfArgumentsInMethod(function.name, _returnType);
        _returnType = ReturnType.Cursor;
    }

    /**
     * Method for updating command by function which argument should be a document
     * @param function inputted function
     * @throws ParseException when function is called with invalid number of arguments, or some of the arguments couldn't be parsed
     */
    private void updateWithCursorDocumentMethod(FunctionItem function) throws ParseException {
        if (function.args.size() == 1)
            command.put(function.name, function.args.getDocument(0));
        else
            throw exceptionsFactory.invalidNumberOfArgumentsInMethod(function.name, _returnType);
        _returnType = ReturnType.Cursor;
    }

    /**
     * Method for updating command by function which argument should be a string
     * @param function inputted function
     * @throws ParseException when function is called with invalid number of arguments, or some of the arguments couldn't be parsed
     */
    private void updateWithCursorStringMethod(FunctionItem function) throws ParseException {
        if (function.args.size() == 1)
            command.put(function.name, function.args.getString(0));
        else
            throw exceptionsFactory.invalidNumberOfArgumentsInMethod(function.name, _returnType);
        _returnType = ReturnType.Cursor;
    }

    /**
     * Method which updates command by hint function
     * @param function inputted function
     * @throws ParseException when function is called with invalid number of arguments, or some of the arguments couldn't be parsed
     */
    private void updateWithCursorHint(FunctionItem function) throws ParseException {
        if (function.args.size() == 1) {
            try {
                Document hintDocument = function.args.getDocument(0);
                command.put("hint", hintDocument);
            } catch (ParseException e) {
                command.put("hint", function.args.getString(0));
            }
        } else {
            throw exceptionsFactory.invalidNumberOfArgumentsInMethod(function.name, _returnType);
        }
        _returnType = ReturnType.Cursor;
    }

    /**
     * Method which updates command by count function.
     * It is not used, because count produce different format of result and explain tree
     * @param function inputted function
     * @throws ParseException when function is called with invalid number of arguments, or some of the arguments couldn't be parsed
     */
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
                throw exceptionsFactory.invalidCountUsage();
            }

        } else {
            throw exceptionsFactory.invalidNumberOfArgumentsInMethod(function.name, _returnType);
        }
        _returnType = ReturnType.None;
    }

    /**
     * Method that decides which types of cursor method is used to update actual command
     * @param function inputted function
     * @throws ParseException when function does not exist or is not supported
     */
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
                throw exceptionsFactory.notSupportedMethod("count", _returnType);
            default:
                throw exceptionsFactory.invalidMethod(function.name, _returnType);
        }
    }
}
