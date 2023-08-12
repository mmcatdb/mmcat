package cz.matfyz.abstractwrappers.utils;

/**
 * @author jachym.bartik
 */
public class PullQuery {

    private String stringContent = null;

    public String getStringContent() {
        return stringContent;
    }

    public boolean hasStringContent() {
        return stringContent != null;
    }

    private String kindName = null;

    public String getKindName() {
        return kindName;
    }

    public boolean hasKindName() {
        return kindName != null;
    }
    
    private Integer offset = null;

    public int getOffset() {
        return offset;
    }

    public boolean hasOffset() {
        return offset != null;
    }

    private Integer limit = null;

    public int getLimit() {
        return limit;
    }

    public boolean hasLimit() {
        return limit != null;
    }

    private PullQuery(String stringContent, String kindName, Integer offset, Integer limit) {
        this.stringContent = stringContent;
        this.kindName = kindName;
        this.offset = offset;
        this.limit = limit;
    }

    public static PullQuery fromString(String stringContent) {
        return new PullQuery(stringContent, null, null, null);
    }

    public static PullQuery fromKindName(String kindName) {
        return new PullQuery(null, kindName, null, null);
    }

    public static Builder withOffset(Integer offset) {
        return new Builder().withOffset(offset);
    }

    public static Builder withLimit(Integer limit) {
        return new Builder().withLimit(limit);
    }

    public static class Builder {

        private Integer offset = null;
        private Integer limit = null;

        public PullQuery fromKindName(String kindName) {
            return new PullQuery(null, kindName, offset, limit);
        }

        public Builder withOffset(Integer offset) {
            this.offset = offset;
            return this;
        }

        public Builder withLimit(Integer limit) {
            this.limit = limit;
            return this;
        }

    }

}
