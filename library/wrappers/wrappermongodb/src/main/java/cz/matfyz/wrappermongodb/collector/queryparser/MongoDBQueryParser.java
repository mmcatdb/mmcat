package cz.matfyz.wrappermongodb.collector.queryparser;

import cz.matfyz.abstractwrappers.exception.collector.ParseException;
import org.bson.Document;


/**
 * Main class handling all process of parsing mongo query to correct mongo command
 */
public class MongoDBQueryParser {

    /**
     * Method which will split query into tokens for easier parsing
     * @param query inputted query
     * @return instance of parsed tokens
     * @throws ParseException when some problem occur during parsing process
     */
    private QueryTokens splitToTokens(String query) throws ParseException {
        StringBuilder buffer = new StringBuilder();
        QueryTokens.Builder tokensBuilder = new QueryTokens.Builder();

        boolean isInsideArgs = false;

        for (char ch : query.toCharArray()) {
            if (isInsideArgs) {
                if (ch == ')')
                    isInsideArgs = false;
                buffer.append(ch);
            } else {
                if (ch == '.') {
                    tokensBuilder.addToken(buffer.toString());
                    buffer.setLength(0);
                } else {
                    if (ch == '(')
                        isInsideArgs = true;
                    buffer.append(ch);
                }
            }
        }

        if (!buffer.isEmpty())
            tokensBuilder.addToken(buffer.toString());

        return tokensBuilder.toTokens();
    }

    /**
     * Main function which parse query to command
     * @param query query to be parsed
     * @return parsed command
     * @throws ParseException when some ParseException occur during parsing process
     */
    public Document parseQueryToCommand(String query) throws ParseException {
        QueryTokens tokens = splitToTokens(query);
        CommandBuilder commandBuilder = new CommandBuilder(tokens.collectionName);

        while(tokens.moveNext()) {
            commandBuilder.updateWithFunction(tokens.getActualFunction());
        }
        return commandBuilder.build();
    }
}
