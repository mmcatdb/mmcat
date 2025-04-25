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
        final var user = builder.value(Schema.userToUserId, userIdValue).object(Schema.user);

        builder.morphism(Schema.userToUserId, user, builder.valueObject(Schema.userId, userIdValue));
        builder.morphism(Schema.userToName, user, builder.valueObject(Schema.name, nameValue));
        builder.morphism(Schema.userToCreatedAt, user, builder.valueObject(Schema.createdAt, createdAtValue));
        builder.morphism(Schema.userToFans, user, builder.valueObject(Schema.fans, fansValue));
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

        final var comment = builder.value(Schema.commentToCommentId, commentIdValue).object(Schema.comment);

        builder.morphism(Schema.commentToCommentId, comment, builder.valueObject(Schema.commentId, commentIdValue));
        builder.morphism(Schema.commentToBusiness, comment, business);
        builder.morphism(Schema.commentToUser, comment, user);
        builder.morphism(Schema.commentToDate, comment, builder.valueObject(Schema.date, dateValue));
        builder.morphism(Schema.commentToText, comment, builder.valueObject(Schema.text, textValue));
        builder.morphism(Schema.commentToStars, comment, builder.valueObject(Schema.stars, starsValue));
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

        final var businessHours = builder.value(Schema.businessHoursToBusinessHoursId, businessHoursIdValue).object(Schema.businessHours);

        builder.morphism(Schema.businessHoursToBusinessHoursId, businessHours, builder.valueObject(Schema.businessHoursId, businessHoursIdValue));
        builder.morphism(Schema.businessHoursToBusiness, businessHours, business);
        builder.morphism(Schema.businessHoursToHours, businessHours, builder.valueObject(Schema.hours, hoursValue));
    }

}
