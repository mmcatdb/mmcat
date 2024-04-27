package cz.matfyz.abstractwrappers.querycontent;

public class StringQuery implements QueryContent {

    public final String content;

    public StringQuery(String content) {
        this.content = content;
    }

    @Override public String toString() {
        return content;
    }

}
