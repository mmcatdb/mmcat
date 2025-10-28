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
                b.simple("user_id", Schema.userToUserId),
                b.simple("name", Schema.userToName),
                b.simple("created_at", Schema.userToCreatedAt),
                b.simple("fans", Schema.userToFans)
            )
        );
    }

    public static void addUser(InstanceBuilder builder, String userIdValue, String nameValue, String createdAtValue, String fansValue) {
        builder
            .value(Schema.userToUserId, userIdValue)
            .value(Schema.userToName, nameValue)
            .value(Schema.userToCreatedAt, createdAtValue)
            .value(Schema.userToFans, fansValue)
            .objex(Schema.user);
    }

    public static TestMapping comment(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.comment,
            commentKind,
            b -> b.root(
                b.simple("comment_id", Schema.commentToCommentId),
                b.simple("business_id", Schema.commentToBusinessId),
                b.simple("user_id", Schema.commentToUserId),
                b.simple("date", Schema.commentToDate),
                b.simple("text", Schema.commentToText),
                b.simple("stars", Schema.commentToStars)
            )
        );
    }

    public static void addComment(InstanceBuilder builder, String commentIdValue, int businessIndex, int userIndex, String dateValue, String textValue, String starsValue) {
        final var user = builder.getRow(Schema.user, userIndex);
        final var business = builder.getRow(Schema.business, businessIndex);

        final var comment = builder
            .value(Schema.commentToCommentId, commentIdValue)
            .value(Schema.commentToDate, dateValue)
            .value(Schema.commentToText, textValue)
            .value(Schema.commentToStars, starsValue)
            .objex(Schema.comment);

        builder.morphism(Schema.commentToBusiness, comment, business);
        builder.morphism(Schema.commentToUser, comment, user);
    }

    public static TestMapping businessHours(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.businessHours,
            businessHoursKind,
            b -> b.root(
                b.simple("business_hours_id", Schema.businessHoursToBusinessHoursId),
                b.simple("business_id", Schema.businessHoursToBusinessId),
                b.simple("hours", Schema.businessHoursToHours)
            )
        );
    }

    public static void addBusinessHours(InstanceBuilder builder, String businessHoursIdValue, int businessIndex, String hoursValue) {
        final var business = builder.getRow(Schema.business, businessIndex);

        final var businessHours = builder
            .value(Schema.businessHoursToBusinessHoursId, businessHoursIdValue)
            .value(Schema.businessHoursToHours, hoursValue)
            .objex(Schema.businessHours);

        builder.morphism(Schema.businessHoursToBusiness, businessHours, business);
    }

}
