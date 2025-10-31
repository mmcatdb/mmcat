package cz.matfyz.tests.example.adminer;

import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.datasource.Datasource.DatasourceType;
import cz.matfyz.core.instance.InstanceBuilder;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.common.TestMapping;

public abstract class PostgreSQL {

    private PostgreSQL() {}

    public static final Datasource datasource = new Datasource(DatasourceType.postgresql, "postgresql");

    public static final String userKind = "user";
    public static final String commentKind = "comment";
    public static final String businessHoursKind = "business_hours";


    public static TestMapping user(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.user,
            userKind,
            b -> b.root(
                b.simple("user_id", Schema.user_userId),
                b.simple("name", Schema.user_name),
                b.simple("created_at", Schema.user_createdAt),
                b.simple("fans", Schema.user_fans)
            )
        );
    }

    public static void addUser(InstanceBuilder builder, String userIdValue, String nameValue, String createdAtValue, String fansValue) {
        builder
            .value(Schema.user_userId, userIdValue)
            .value(Schema.user_name, nameValue)
            .value(Schema.user_createdAt, createdAtValue)
            .value(Schema.user_fans, fansValue)
            .objex(Schema.user);
    }

    public static TestMapping comment(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.comment,
            commentKind,
            b -> b.root(
                b.simple("comment_id", Schema.comment_commentId),
                b.simple("business_id", Schema.comment_businessId),
                b.simple("user_id", Schema.comment_userId),
                b.simple("date", Schema.comment_date),
                b.simple("text", Schema.comment_text),
                b.simple("stars", Schema.comment_stars)
            )
        );
    }

    public static void addComment(InstanceBuilder builder, String commentIdValue, int businessIndex, int userIndex, String dateValue, String textValue, String starsValue) {
        final var user = builder.getRow(Schema.user, userIndex);
        final var business = builder.getRow(Schema.business, businessIndex);

        final var comment = builder
            .value(Schema.comment_commentId, commentIdValue)
            .value(Schema.comment_date, dateValue)
            .value(Schema.comment_text, textValue)
            .value(Schema.comment_stars, starsValue)
            .objex(Schema.comment);

        builder.morphism(Schema.comment_business, comment, business);
        builder.morphism(Schema.comment_user, comment, user);
    }

    public static TestMapping businessHours(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.businessHours,
            businessHoursKind,
            b -> b.root(
                b.simple("business_hours_id", Schema.businessHours_businessHoursId),
                b.simple("business_id", Schema.businessHours_businessId),
                b.simple("hours", Schema.businessHours_hours)
            )
        );
    }

    public static void addBusinessHours(InstanceBuilder builder, String businessHoursIdValue, int businessIndex, String hoursValue) {
        final var business = builder.getRow(Schema.business, businessIndex);

        final var businessHours = builder
            .value(Schema.businessHours_businessHoursId, businessHoursIdValue)
            .value(Schema.businessHours_hours, hoursValue)
            .objex(Schema.businessHours);

        builder.morphism(Schema.businessHours_business, businessHours, business);
    }

}
