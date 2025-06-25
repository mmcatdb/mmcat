package cz.matfyz.wrappermongodb.collector;

import cz.matfyz.abstractwrappers.exception.collector.DataCollectException;
import cz.matfyz.abstractwrappers.exception.collector.ParseException;
import cz.matfyz.abstractwrappers.exception.collector.WrapperExceptionsFactory;
import cz.matfyz.wrappermongodb.collector.queryparser.CommandBuilder;

public class
MongoExceptionsFactory extends WrapperExceptionsFactory {
    //region ParseException initialization
    public ParseException documentKeyNotFound(String key) {
        var message = new Message("key '" + key + "' was not present in document").toString();
        return new ParseException(message);
    }

    public ParseException invalidNumberOfArgumentsInMethod(String methodName, CommandBuilder.ReturnType type) {
        String message;
        if (type == CommandBuilder.ReturnType.Collection)
            message = new Message("collection method '" + methodName + "' has invalid number of arguments. At most two are expected to be present").toString();
        else if (type == CommandBuilder.ReturnType.Cursor)
            message = new Message("cursor method '" + methodName + "' has invalid number of arguments. At most two are expected to be present").toString();
        else
            message = new Message("method '" + methodName + "' has invalid number of arguments. At most two are expected to be present.").toString();
        return new ParseException(message);
    }

    public ParseException notSupportedMethod(String methodName, CommandBuilder.ReturnType type) {
        String message;
        if (type == CommandBuilder.ReturnType.Collection)
            message = new Message("collection method '" + methodName + "' is not supported by system").toString();
        else if (type == CommandBuilder.ReturnType.Cursor)
            message = new Message("cursor method '" + methodName + "' is not supported by system").toString();
        else
            message = new Message("method '" + methodName + "' is not supported by system").toString();
        return new ParseException(message);
    }

    public ParseException invalidMethod(String methodName, CommandBuilder.ReturnType type) {
        String message;
        if (type == CommandBuilder.ReturnType.Collection)
            message = new Message("collection method '" + methodName + "' does not exist").toString();
        else if (type == CommandBuilder.ReturnType.Cursor)
            message = new Message("cursor method '" + methodName + "' does not exist").toString();
        else
            message = new Message("method '" + methodName + "' does not exist").toString();
        return new ParseException(message);
    }

    public ParseException invalidMethodOption(String optionName, String methodName, CommandBuilder.ReturnType type) {
        String message;
        if (type == CommandBuilder.ReturnType.Collection)
            message = new Message("option '" + optionName + "' in collection method '" + methodName + "' is not supported or does not exist").toString();
        else if (type == CommandBuilder.ReturnType.Cursor)
            message = new Message("option '" + optionName + "' in cursor method '" + methodName + "' is not supported or does not exist").toString();
        else
            message = new Message("option '" + optionName + "' in method '" + methodName + "' is not supported or does not exist").toString();
        return new ParseException(message);
    }

    public ParseException invalidCountUsage() {
        String message = new Message("count method can be used on find method only").toString();
        return new ParseException(message);
    }
    //endregion

    public DataCollectException collectionNotParsed() {
        var message = new Message("no collection was parsed from explain plan").toString();
        return new DataCollectException(message);
    }

    public static MongoExceptionsFactory getExceptionsFactory() { return new MongoExceptionsFactory(); }
}
