package cz.matfyz.abstractwrappers.querycontent;

/**
 * @author jachym.bartik
 */
public class StringQuery implements QueryContent {

    public final String content;

    public StringQuery(String content) {
        this.content = content;
    }

}
