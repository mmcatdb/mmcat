package cz.matfyz.evolution.querying;

import cz.matfyz.core.exception.NamedException;
import cz.matfyz.core.exception.OtherException;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.evolution.querying.QueryUpdateResult.ErrorType;
import cz.matfyz.evolution.querying.QueryUpdateResult.QueryUpdateError;

import java.util.List;

public class QueryEvolver {
    
    private SchemaCategory prevCategory;
    private SchemaCategory nextCategory;

    public QueryEvolver(SchemaCategory prevCategory, SchemaCategory nextCategory) {
        this.prevCategory = prevCategory;
        this.nextCategory = nextCategory;
    }

    public QueryUpdateResult run(String prevContent) {
        try {
            return innerRun(prevContent);
        }
        catch (NamedException e) {
            throw e;
        }
        catch (Exception e) {
            throw new OtherException(e);
        }
    }

    private QueryUpdateResult innerRun(String prevContent) throws Exception {
        return new QueryUpdateResult(prevContent, List.of(
            new QueryUpdateError(ErrorType.UpdateError, "Unexpected error in the query", null)
        ));
    }

}
