package cz.matfyz.tests.example.benchmark.yelp;

import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.datasource.Datasource.DatasourceType;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.common.TestMapping;

public abstract class PostgreSQL {

    private PostgreSQL() {}

    public static final Datasource datasource = new Datasource(DatasourceType.postgresql, "postgresql");

    public static final String businessKind = "business";
    public static final String userKind = "yelp_user";
    public static final String reviewKind = "review";
    public static final String isFriendKind = "friendship";

    public static TestMapping business(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.business,
            businessKind,
            b -> b.root(
                b.simple("business_id", Schema.business_id),
                b.simple("name", Schema.business_name),
                b.simple("city", Schema.business_city),
                b.simple("state", Schema.business_state),
                // b.simple("stars", Schema.business_stars),
                // b.simple("review_count", Schema.business_revCnt),
                b.simple("is_open", Schema.business_isOpen)
            )
        );
    }

    public static TestMapping user(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.user,
            userKind,
            b -> b.root(
                b.simple("user_id", Schema.user_id),
                b.simple("name", Schema.user_name),
                // b.simple("review_count", Schema.user_reviewCount),
                b.simple("yelping_since", Schema.user_yelpingSince)
                // b.simple("useful", Schema.user_useful),
                // b.simple("funny", Schema.user_funny),
                // b.simple("cool", Schema.user_cool)
            )
        );
    }

    // TODO: check that this works!
    public static TestMapping friendship(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.user,
            isFriendKind,
            b -> b.root(
                b.simple("user_id", Schema.user_id),
                b.simple("friend_id", Schema.friendship_user1.dual().concatenate(Schema.friendship_user2.signature()).concatenate(Schema.user_id.signature()))
            )
        );
    }

    public static TestMapping review(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.review,
            reviewKind,
            b -> b.root(
                b.simple("review_id", Schema.review_id),
                b.simple("user_id", Schema.review_user.signature().concatenate(Schema.user_id.signature())),
                b.simple("business_id", Schema.review_business.signature().concatenate(Schema.business_id.signature())),
                b.simple("stars", Schema.review_stars),
                b.simple("date", Schema.review_date),
                b.simple("useful", Schema.review_useful),
                b.simple("funny", Schema.review_funny),
                b.simple("cool", Schema.review_cool)
            )
        );
    }

}
