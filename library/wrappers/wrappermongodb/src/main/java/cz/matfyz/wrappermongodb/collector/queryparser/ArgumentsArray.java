package cz.matfyz.wrappermongodb.collector.queryparser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.matfyz.abstractwrappers.exception.collector.ParseException;

import org.bson.Document;
import org.bson.json.JsonParseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class that represents array of parsed arguments of functions from mongosh query language
 */
public class ArgumentsArray {

    private static String getErrorMessageForInvalidType(String expectedType, String givenValue) {
        return "Argument was expected to be " + expectedType + ". Instead " + givenValue + " was given.";
    }

    /** Field holding private container of arguments*/
    private String[] _array;

    private ArgumentsArray(String[] array) {
        _array = array;
    }

    /**
     * Get value as string on some position
     * @param index to specify position
     * @return gotten value as index
     */
    public String getString(int index){
        return _array[index];
    }

    /**
     * Get value as org.bson.Document on some position
     * @param index to specify position
     * @return gotten value as Document
     * @throws ParseException if accessed value cannot be parsed to Document
     */
    public Document getDocument(int index) throws ParseException {
        try {
            return Document.parse(_array[index]);
        } catch (JsonParseException e){
            throw new ParseException(getErrorMessageForInvalidType("Document", _array[index]), e);
        }

    }

    /**
     * Get value as Document list on some position
     * @param index to specify which position to access
     * @return parsed document list value
     * @throws ParseException when value cannot be parsed as document list
     */
    public List<Document> getDocumentList(int index) throws ParseException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return Arrays.stream(mapper.readValue(_array[index], Document[].class)).toList();
        } catch (JsonProcessingException e) {
            throw new ParseException(getErrorMessageForInvalidType("DocumentArray", _array[index]), e);
        }
    }

    /**
     * Method for getting value as boolean on specified position
     * @param index to select position
     * @return true if value is parsed as true
     */
    public boolean getBoolean(int index) {
        return Boolean.parseBoolean(_array[index]);
    }

    /**
     * Method for getting value as integer on specified index
     * @param index specified index
     * @return value as integer
     * @throws ParseException when value cannot be parsed to int
     */
    public int getInteger(int index) throws ParseException {
        try {
            return Integer.parseInt(_array[index]);
        } catch (NumberFormatException e) {
            throw new ParseException(getErrorMessageForInvalidType("Integer", _array[index]), e);
        }

    }

    /**
     * Method for getting instance of value as double
     * @param index to select position of value
     * @return parsed double value
     * @throws ParseException when value cannot be parsed as double
     */
    public double getDouble(int index) throws ParseException {
        try {
            return Double.parseDouble(_array[index]);
        } catch (NumberFormatException e) {
            throw new ParseException(getErrorMessageForInvalidType("Double", _array[index]), e);
        }
    }

    /**
     * Getter for size of ArrayArguments
     * @return the size of it
     */
    public int size() {
        return _array.length;
    }

    /**
     * Private enum for storing in which state or type parsing is during the parse process of arguments
     */
    private enum ArgParseType {
        InObject,
        InArray,
        InString,
        InDoubleString,
        InNumber,
        InBoolean,
        Out
    }

    /**
     * Class which holds actual state of parsing
     */
    private static class ArgParseState {
        public ArgParseType type;
        public int indentation;
        public char prevChar;

        public ArgParseState() {
            type = ArgParseType.Out;
            indentation = 0;
            prevChar = '\0';
        }
    }

    /**
     * Static method which gets content of arguments as string and parse them to instance of this class
     * @param argContent content which is string in between parentheses when mongosh function is parsed
     * @return instance of ArgumentsArray class
     */
    public static ArgumentsArray parseArguments(String argContent) {
        if (argContent == null)
            return new ArgumentsArray(new String[0]);

        String content = argContent.trim();
        ArgParseState state = new ArgParseState();

        StringBuilder buffer = new StringBuilder();
        List<String> args = new ArrayList<>();

        for (char ch : content.toCharArray()) {
            if (state.type == ArgParseType.Out) {
                if (ch == '"')
                    state.type = ArgParseType.InDoubleString;
                else if (ch == '\'')
                    state.type = ArgParseType.InString;
                else if (ch == '{') {
                    state.type = ArgParseType.InObject;
                    buffer.append(ch);
                } else if (ch == '[') {
                    state.type = ArgParseType.InArray;
                    buffer.append(ch);
                } else if (ch == ',') {
                    args.add(buffer.toString());
                    buffer.setLength(0);
                } else if (Character.isDigit(ch)) {
                    state.type = ArgParseType.InNumber;
                    buffer.append(ch);
                } else if (!Character.isWhitespace(ch)) {
                    state.type = ArgParseType.InBoolean;
                    buffer.append(ch);
                }

            } else if (state.type == ArgParseType.InObject) {
                if (ch == '{')
                    state.indentation += 1;
                else if (ch == '}' && state.indentation == 0) {
                    state.type = ArgParseType.Out;
                } else if (ch == '}')
                    state.indentation -= 1;
                buffer.append(ch);
            } else if (state.type == ArgParseType.InArray) {
                if (ch == '[')
                    state.indentation += 1;
                else if (ch == ']' && state.indentation == 0) {
                    state.type = ArgParseType.Out;
                } else if (ch == ']')
                        state.indentation -= 1;
                buffer.append(ch);
            } else if (state.type == ArgParseType.InString) {
                if (ch == '\'' && state.prevChar != '\\')
                    state.type = ArgParseType.Out;
                else {
                    buffer.append(ch);
                }
            } else if (state.type == ArgParseType.InDoubleString) {
                if (ch == '"' && state.prevChar != '\\')
                    state.type = ArgParseType.Out;
                else {
                    buffer.append(ch);
                }
            } else if (state.type == ArgParseType.InNumber) {
                if (Character.isWhitespace(ch)) {
                    state.type = ArgParseType.Out;
                } else
                    buffer.append(ch);
            } else if (state.type == ArgParseType.InBoolean) {
                if (Character.isWhitespace(ch)) {
                    state.type = ArgParseType.Out;
                } else {
                    buffer.append(ch);
                }
            }
            state.prevChar = ch;
        }

        if (!buffer.isEmpty()) {
            args.add(buffer.toString());
        }

        return new ArgumentsArray(args.toArray(String[]::new));
    }

    /**
     * Method which returns string representation of this class
     * @return String representation of instance
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < _array.length; i++) {
            buffer.append(_array[i]);
            if (i < _array.length - 1)
                buffer.append(", ");
        }
        return buffer.toString();
    }
 }
