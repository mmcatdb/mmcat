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

    public String getString(int index){
        return _array[index];
    }

    public Document getDocument(int index) throws ParseException {
        try {
            return Document.parse(_array[index]);
        } catch (JsonParseException e){
            throw new ParseException(getErrorMessageForInvalidType("Document", _array[index]), e);
        }

    }

    public List<Document> getDocumentList(int index) throws ParseException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return Arrays.stream(mapper.readValue(_array[index], Document[].class)).toList();
        } catch (JsonProcessingException e) {
            throw new ParseException(getErrorMessageForInvalidType("DocumentArray", _array[index]), e);
        }
    }

    public boolean getBoolean(int index) {
        return Boolean.parseBoolean(_array[index]);
    }

    public int getInteger(int index) throws ParseException {
        try {
            return Integer.parseInt(_array[index]);
        } catch (NumberFormatException e) {
            throw new ParseException(getErrorMessageForInvalidType("Integer", _array[index]), e);
        }

    }

    public double getDouble(int index) throws ParseException {
        try {
            return Double.parseDouble(_array[index]);
        } catch (NumberFormatException e) {
            throw new ParseException(getErrorMessageForInvalidType("Double", _array[index]), e);
        }
    }

    public int size() {
        return _array.length;
    }

    /** Stores in which state or type parsing is during the parse process of arguments. */
    private enum ArgParseType {
        InObject,
        InArray,
        InString,
        InDoubleString,
        InNumber,
        InBoolean,
        Out
    }

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
     * Gets content of arguments as string and parse them to instance of this class.
     * @param argContent content which is string in between parentheses when mongosh function is parsed
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

    @Override public String toString() {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < _array.length; i++) {
            buffer.append(_array[i]);
            if (i < _array.length - 1)
                buffer.append(", ");
        }
        return buffer.toString();
    }
 }
